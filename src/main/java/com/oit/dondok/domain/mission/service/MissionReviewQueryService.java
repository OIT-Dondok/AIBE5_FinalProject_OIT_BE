package com.oit.dondok.domain.mission.service;

import com.oit.dondok.domain.crew.entity.Crew;
import com.oit.dondok.domain.crew.entity.CrewStatus;
import com.oit.dondok.domain.crew.exception.CrewErrorCode;
import com.oit.dondok.domain.crew.repository.CrewRepository;
import com.oit.dondok.domain.image.port.ImageDeliveryPort;
import com.oit.dondok.domain.image.port.ImageObjectKey;
import com.oit.dondok.domain.mission.dto.response.MissionReviewListResponse;
import com.oit.dondok.domain.mission.dto.response.MissionReviewSummaryItemResponse;
import com.oit.dondok.domain.mission.dto.response.MissionReviewSummaryItemResponse.MissionReviewCategory;
import com.oit.dondok.domain.mission.dto.response.MissionReviewSummaryResponse;
import com.oit.dondok.domain.mission.entity.DailySettlementType;
import com.oit.dondok.domain.mission.entity.MissionRule;
import com.oit.dondok.domain.mission.exception.MissionErrorCode;
import com.oit.dondok.domain.mission.repository.MissionLogQueryRepository;
import com.oit.dondok.domain.mission.repository.MissionLogQueryRepository.MissionReviewItemProjection;
import com.oit.dondok.domain.mission.repository.MissionLogQueryRepository.MissionReviewSummaryCount;
import com.oit.dondok.domain.mission.repository.MissionRuleRepository;
import com.oit.dondok.global.exception.CustomException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MissionReviewQueryService {

  private static final int MAX_LIMIT = 100;
  private static final Duration IMAGE_URL_TTL = Duration.ofMinutes(10);
  private static final Duration HOST_REVIEW_GRACE_PERIOD = Duration.ofHours(72);
  private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");
  private static final String CURSOR_VERSION = "v1";
  private static final String CURSOR_DELIMITER = "|";

  private final CrewRepository crewRepository;
  private final MissionRuleRepository missionRuleRepository;
  private final MissionLogQueryRepository missionLogQueryRepository;
  private final ImageDeliveryPort imageDeliveryPort;

  @Transactional(readOnly = true)
  public MissionReviewListResponse getReviewList(
      UUID memberUuid, Long crewId, String reviewCategory, String cursor, int limit) {
    Crew crew =
        crewRepository
            .findWithHostMemberById(crewId)
            .orElseThrow(() -> new CustomException(CrewErrorCode.CREW_NOT_FOUND));
    validateHost(memberUuid, crew);
    validateCrewActive(crew);

    MissionRule missionRule =
        missionRuleRepository
            .findByCrewId(crewId)
            .orElseThrow(() -> new CustomException(MissionErrorCode.MISSION_RULE_NOT_FOUND));

    MissionReviewCategory categoryFilter = parseCategory(reviewCategory);
    int effectiveLimit = Math.min(Math.max(limit, 1), MAX_LIMIT);
    Cursor cursorState = parseCursor(cursor, categoryFilter);
    LocalDateTime now = LocalDateTime.now(SEOUL_ZONE);
    DailySettlementType dailySettlementType = missionRule.getDailySettlementType();
    LocalDateTime reviewWindowStart = resolveReviewWindowStart(dailySettlementType, now);

    MissionReviewSummaryResponse summary =
        createSummary(
            missionLogQueryRepository.countReviewCandidatesByCategory(
                crewId, reviewWindowStart, dailySettlementType, now));

    List<MissionReviewItemProjection> rows =
        missionLogQueryRepository.findReviewItemsByCursor(
            crewId,
            reviewWindowStart,
            dailySettlementType,
            categoryFilter,
            now,
            effectiveLimit + 1,
            cursorState.serverTime(),
            cursorState.missionLogId());

    boolean hasNext = rows.size() > effectiveLimit;
    List<MissionReviewItemProjection> pageItems = hasNext ? rows.subList(0, effectiveLimit) : rows;
    String nextCursor =
        hasNext
            ? toCursor(
                categoryFilter,
                reviewDeadlineAt(
                    dailySettlementType, pageItems.get(pageItems.size() - 1).serverTime()),
                pageItems.get(pageItems.size() - 1).serverTime(),
                pageItems.get(pageItems.size() - 1).missionLogId())
            : null;

    return new MissionReviewListResponse(
        pageItems.stream()
            .map(row -> buildResponse(row, categoryFilter, dailySettlementType))
            .toList(),
        summary,
        nextCursor);
  }

  private void validateHost(UUID memberUuid, Crew crew) {
    if (!crew.getHostMember().getUuid().equals(memberUuid)) {
      throw new CustomException(MissionErrorCode.FORBIDDEN_NOT_HOST);
    }
  }

  private void validateCrewActive(Crew crew) {
    if (crew.getStatus() != CrewStatus.ACTIVE) {
      throw new CustomException(MissionErrorCode.MISSION_REVIEW_NOT_AVAILABLE_FOR_CREW_STATUS);
    }
  }

  private MissionReviewCategory parseCategory(String reviewCategory) {
    if (reviewCategory == null || reviewCategory.isBlank()) {
      throw new CustomException(MissionErrorCode.INVALID_REVIEW_CATEGORY);
    }
    try {
      return MissionReviewCategory.valueOf(reviewCategory);
    } catch (IllegalArgumentException exception) {
      throw new CustomException(MissionErrorCode.INVALID_REVIEW_CATEGORY);
    }
  }

  private LocalDateTime resolveReviewWindowStart(
      DailySettlementType dailySettlementType, LocalDateTime now) {
    LocalDate today = now.toLocalDate();
    LocalDate earliestDate = today;

    for (LocalDate missionDate = today.minusDays(5);
        !missionDate.isAfter(today);
        missionDate = missionDate.plusDays(1)) {
      LocalDateTime reviewExpiresAt =
          dailySettlementType.autoCertificationAt(missionDate).plus(HOST_REVIEW_GRACE_PERIOD);
      if (!now.isAfter(reviewExpiresAt)) {
        earliestDate = missionDate;
        break;
      }
    }

    return earliestDate.atStartOfDay();
  }

  private MissionReviewSummaryItemResponse buildResponse(
      MissionReviewItemProjection item,
      MissionReviewCategory reviewCategory,
      DailySettlementType dailySettlementType) {
    return new MissionReviewSummaryItemResponse(
        item.missionLogId(),
        item.crewId(),
        item.crewParticipantId(),
        item.submitterMemberUuid(),
        item.submitterNickname(),
        createImageUrl(item.imageS3Key()),
        item.caption(),
        toSeoulOffset(item.serverTime()),
        toSeoulOffset(item.exifTakenAt()),
        item.exifRisk(),
        item.duplicateHash(),
        item.certificationStatus(),
        item.decisionType(),
        item.rejectReasonCode(),
        reviewCategory,
        toSeoulOffset(reviewDeadlineAt(dailySettlementType, item.serverTime())));
  }

  private String createImageUrl(String imageS3Key) {
    return imageDeliveryPort.createDeliveryUrl(new ImageObjectKey(imageS3Key), IMAGE_URL_TTL).url();
  }

  private MissionReviewSummaryResponse createSummary(MissionReviewSummaryCount count) {
    return new MissionReviewSummaryResponse(
        count.urgentCount(), count.cautionCount(), count.normalCount());
  }

  private Cursor parseCursor(String cursor, MissionReviewCategory reviewCategory) {
    if (cursor == null || cursor.isBlank()) {
      return new Cursor(null, null);
    }

    try {
      String decoded =
          new String(
              Base64.getUrlDecoder().decode(restoreBase64Padding(cursor.trim())),
              StandardCharsets.UTF_8);
      String[] parts = decoded.split("\\|", -1);
      if (parts.length != 5 || !CURSOR_VERSION.equals(parts[0])) {
        throw new CustomException(MissionErrorCode.INVALID_CURSOR);
      }

      MissionReviewCategory cursorCategory = MissionReviewCategory.valueOf(parts[1]);
      if (cursorCategory != reviewCategory) {
        throw new CustomException(MissionErrorCode.INVALID_CURSOR);
      }

      OffsetDateTime.parse(parts[2]);
      LocalDateTime serverTime =
          OffsetDateTime.parse(parts[3]).atZoneSameInstant(SEOUL_ZONE).toLocalDateTime();
      Long missionLogId = Long.parseLong(parts[4]);
      return new Cursor(serverTime, missionLogId);
    } catch (DateTimeParseException | IllegalArgumentException exception) {
      throw new CustomException(MissionErrorCode.INVALID_CURSOR);
    }
  }

  private String toCursor(
      MissionReviewCategory reviewCategory,
      LocalDateTime reviewDeadlineAt,
      LocalDateTime serverTime,
      Long missionLogId) {
    String payload =
        String.join(
            CURSOR_DELIMITER,
            CURSOR_VERSION,
            reviewCategory.name(),
            toSeoulOffset(reviewDeadlineAt).toString(),
            toSeoulOffset(serverTime).toString(),
            missionLogId.toString());
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
  }

  private String restoreBase64Padding(String cursor) {
    int remainder = cursor.length() % 4;
    if (remainder == 0) {
      return cursor;
    }
    if (remainder == 1) {
      throw new CustomException(MissionErrorCode.INVALID_CURSOR);
    }
    return cursor + "=".repeat(4 - remainder);
  }

  private LocalDateTime reviewDeadlineAt(
      DailySettlementType dailySettlementType, LocalDateTime serverTime) {
    return dailySettlementType.autoCertificationAt(serverTime.toLocalDate());
  }

  private OffsetDateTime toSeoulOffset(LocalDateTime localDateTime) {
    if (localDateTime == null) {
      return null;
    }
    return localDateTime.atZone(SEOUL_ZONE).toOffsetDateTime();
  }

  private record Cursor(LocalDateTime serverTime, Long missionLogId) {}
}
