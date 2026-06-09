package com.oit.dondok.domain.mission.service;

import com.oit.dondok.domain.crew.entity.Crew;
import com.oit.dondok.domain.crew.exception.CrewErrorCode;
import com.oit.dondok.domain.crew.repository.CrewRepository;
import com.oit.dondok.domain.image.port.ImageDeliveryPort;
import com.oit.dondok.domain.image.port.ImageObjectKey;
import com.oit.dondok.domain.mission.dto.response.MissionReviewListResponse;
import com.oit.dondok.domain.mission.dto.response.MissionReviewSummaryItemResponse;
import com.oit.dondok.domain.mission.dto.response.MissionReviewSummaryResponse;
import com.oit.dondok.domain.mission.entity.CertificationStatus;
import com.oit.dondok.domain.mission.entity.DailySettlementType;
import com.oit.dondok.domain.mission.entity.ExifRisk;
import com.oit.dondok.domain.mission.entity.MissionLog;
import com.oit.dondok.domain.mission.entity.MissionReviewCategory;
import com.oit.dondok.domain.mission.entity.MissionRule;
import com.oit.dondok.domain.mission.entity.ModerationDecisionType;
import com.oit.dondok.domain.mission.exception.MissionErrorCode;
import com.oit.dondok.domain.mission.repository.MissionLogQueryRepository;
import com.oit.dondok.domain.mission.repository.MissionRuleRepository;
import com.oit.dondok.global.exception.CustomException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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

  private final CrewRepository crewRepository;
  private final MissionRuleRepository missionRuleRepository;
  private final MissionLogQueryRepository missionLogQueryRepository;
  private final ImageDeliveryPort imageDeliveryPort;

  @Transactional(readOnly = true)
  public MissionReviewListResponse getReviewList(
      UUID memberUuid, Long crewId, String reviewCategory, String cursor, int limit) {
    Crew crew =
        crewRepository
            .findByIdWithHostMember(crewId)
            .orElseThrow(() -> new CustomException(CrewErrorCode.CREW_NOT_FOUND));
    validateHost(memberUuid, crew);

    MissionRule missionRule =
        missionRuleRepository
            .findByCrewId(crewId)
            .orElseThrow(() -> new CustomException(MissionErrorCode.MISSION_RULE_NOT_FOUND));

    MissionReviewCategory categoryFilter = parseCategory(reviewCategory);
    int effectiveLimit = Math.min(Math.max(limit, 1), MAX_LIMIT);
    Long cursorId = decodeCursor(cursor);
    LocalDateTime now = LocalDateTime.now(SEOUL_ZONE);
    LocalDateTime reviewWindowStart =
        resolveReviewWindowStart(missionRule.getDailySettlementType(), now);

    List<ClassifiedItem> allClassified =
        missionLogQueryRepository.findReviewCandidatesByCrewId(crewId, reviewWindowStart).stream()
            .map(row -> toClassifiedItem(row, missionRule, now))
            .flatMap(Optional::stream)
            .sorted(reviewComparator())
            .toList();

    MissionReviewSummaryResponse summary = createSummary(allClassified);
    List<ClassifiedItem> filteredItems =
        allClassified.stream().filter(item -> item.reviewCategory() == categoryFilter).toList();

    int startIndex = findStartIndex(filteredItems, cursorId);
    int endExclusive = Math.min(startIndex + effectiveLimit, filteredItems.size());
    List<ClassifiedItem> pageItems = filteredItems.subList(startIndex, endExclusive);
    String nextCursor =
        endExclusive < filteredItems.size()
            ? encodeCursor(pageItems.get(pageItems.size() - 1).missionLogId())
            : null;

    return new MissionReviewListResponse(
        pageItems.stream().map(this::buildResponse).toList(), summary, nextCursor);
  }

  // 요청자가 해당 크루의 방장인지 확인한다.
  private void validateHost(UUID memberUuid, Crew crew) {
    if (!crew.getHostMember().getUuid().equals(memberUuid)) {
      throw new CustomException(MissionErrorCode.FORBIDDEN_NOT_HOST);
    }
  }

  // 문자열 query parameter를 검토 분류 enum으로 변환한다.
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

  // 방장 재검토 유예기간 72시간에 걸칠 수 있는 가장 이른 제출일을 조회 하한으로 잡는다.
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

  // DB에 저장하지 않는 review category를 조회 시점 정책으로 계산한다.
  private Optional<ClassifiedItem> toClassifiedItem(
      MissionLogQueryRepository.MissionReviewCandidateRow row,
      MissionRule missionRule,
      LocalDateTime now) {
    MissionLog missionLog = row.missionLog();
    LocalDateTime reviewDeadlineAt =
        missionRule
            .getDailySettlementType()
            .autoCertificationAt(missionLog.getServerTime().toLocalDate());
    MissionReviewCategory category = classify(missionLog, reviewDeadlineAt, now);
    if (category == null) {
      return Optional.empty();
    }

    return Optional.of(new ClassifiedItem(row, category, reviewDeadlineAt));
  }

  // 이미지 접근 URL은 최종 응답 page item에 대해서만 생성한다.
  private MissionReviewSummaryItemResponse buildResponse(ClassifiedItem item) {
    MissionLog missionLog = item.row().missionLog();
    return new MissionReviewSummaryItemResponse(
        missionLog.getId(),
        item.row().crewId(),
        item.row().crewParticipantId(),
        item.row().submitterMemberUuid(),
        item.row().submitterNickname(),
        createImageUrl(missionLog.getImageS3Key()),
        missionLog.getCaption(),
        toSeoulOffset(missionLog.getServerTime()),
        toSeoulOffset(missionLog.getExifTakenAt()),
        missionLog.getExifRisk(),
        missionLog.isDuplicateHash(),
        missionLog.getCertificationStatus(),
        missionLog.getDecisionType(),
        missionLog.getRejectReasonCode(),
        item.reviewCategory(),
        toSeoulOffset(item.reviewDeadlineAt()));
  }

  // 긴급 검토가 EXIF 기반 분류보다 우선한다.
  private MissionReviewCategory classify(
      MissionLog missionLog, LocalDateTime reviewDeadlineAt, LocalDateTime now) {
    if (!now.isBefore(reviewDeadlineAt)
        && isWithinHostReviewGracePeriod(reviewDeadlineAt, now)
        && isNotManuallyReviewed(missionLog)) {
      return MissionReviewCategory.URGENT_REVIEW;
    }

    if (missionLog.getCertificationStatus() != CertificationStatus.PENDING_REVIEW
        || !now.isBefore(reviewDeadlineAt)) {
      return null;
    }

    if (missionLog.getExifRisk() != ExifRisk.NORMAL || missionLog.getExifTakenAt() == null) {
      return MissionReviewCategory.CAUTION_REVIEW;
    }

    return MissionReviewCategory.NORMAL_REVIEW;
  }

  // 검토 기한이 지난 뒤에도 72시간 동안만 긴급 검토에 남긴다.
  private boolean isWithinHostReviewGracePeriod(LocalDateTime reviewDeadlineAt, LocalDateTime now) {
    return !now.isAfter(reviewDeadlineAt.plus(HOST_REVIEW_GRACE_PERIOD));
  }

  // 수동 처리 전이거나 자동 처리된 인증만 검토 목록 후보로 본다.
  private boolean isNotManuallyReviewed(MissionLog missionLog) {
    return missionLog.getCertificationStatus() == CertificationStatus.PENDING_REVIEW
        || missionLog.getDecisionType() == ModerationDecisionType.AUTO_APPROVE
        || missionLog.getDecisionType() == ModerationDecisionType.AUTO_REJECT;
  }

  private String createImageUrl(String imageS3Key) {
    return imageDeliveryPort.createDeliveryUrl(new ImageObjectKey(imageS3Key), IMAGE_URL_TTL).url();
  }

  private MissionReviewSummaryResponse createSummary(List<ClassifiedItem> items) {
    long urgentCount = 0;
    long cautionCount = 0;
    long normalCount = 0;

    for (ClassifiedItem item : items) {
      switch (item.reviewCategory()) {
        case URGENT_REVIEW -> urgentCount++;
        case CAUTION_REVIEW -> cautionCount++;
        case NORMAL_REVIEW -> normalCount++;
      }
    }

    return new MissionReviewSummaryResponse(urgentCount, cautionCount, normalCount);
  }

  private Comparator<ClassifiedItem> reviewComparator() {
    return Comparator.comparingInt((ClassifiedItem item) -> categoryPriority(item.reviewCategory()))
        .thenComparing(this::compareWithinSameCategory);
  }

  // 긴급 검토만 검토 기한을 먼저 보고, 주의/일반 검토는 제출 시각 기준으로 정렬한다.
  private int compareWithinSameCategory(ClassifiedItem left, ClassifiedItem right) {
    if (left.reviewCategory() == MissionReviewCategory.URGENT_REVIEW) {
      int deadlineCompare = left.reviewDeadlineAt().compareTo(right.reviewDeadlineAt());
      if (deadlineCompare != 0) {
        return deadlineCompare;
      }
    }

    int serverTimeCompare = left.serverTime().compareTo(right.serverTime());
    if (serverTimeCompare != 0) {
      return serverTimeCompare;
    }

    return Long.compare(left.missionLogId(), right.missionLogId());
  }

  private int categoryPriority(MissionReviewCategory category) {
    return switch (category) {
      case URGENT_REVIEW -> 0;
      case CAUTION_REVIEW -> 1;
      case NORMAL_REVIEW -> 2;
    };
  }

  private int findStartIndex(List<ClassifiedItem> items, Long cursorId) {
    if (cursorId == null) {
      return 0;
    }
    for (int index = 0; index < items.size(); index++) {
      if (items.get(index).missionLogId().equals(cursorId)) {
        return index + 1;
      }
    }
    return 0;
  }

  private String encodeCursor(Long missionLogId) {
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(String.valueOf(missionLogId).getBytes(StandardCharsets.UTF_8));
  }

  private Long decodeCursor(String cursor) {
    if (cursor == null || cursor.isBlank()) {
      return null;
    }
    try {
      String decoded =
          new String(
              Base64.getUrlDecoder().decode(restoreBase64Padding(cursor)), StandardCharsets.UTF_8);
      return Long.parseLong(decoded);
    } catch (IllegalArgumentException exception) {
      throw new CustomException(CrewErrorCode.INVALID_CURSOR);
    }
  }

  private String restoreBase64Padding(String cursor) {
    int remainder = cursor.length() % 4;
    if (remainder == 0) {
      return cursor;
    }
    return cursor + "=".repeat(4 - remainder);
  }

  private OffsetDateTime toSeoulOffset(LocalDateTime localDateTime) {
    if (localDateTime == null) {
      return null;
    }
    return localDateTime.atZone(SEOUL_ZONE).toOffsetDateTime();
  }

  private record ClassifiedItem(
      MissionLogQueryRepository.MissionReviewCandidateRow row,
      MissionReviewCategory reviewCategory,
      LocalDateTime reviewDeadlineAt) {

    Long missionLogId() {
      return row.missionLog().getId();
    }

    LocalDateTime serverTime() {
      return row.missionLog().getServerTime();
    }
  }
}
