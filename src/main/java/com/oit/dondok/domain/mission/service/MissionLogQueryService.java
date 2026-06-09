package com.oit.dondok.domain.mission.service;

import com.oit.dondok.domain.crew.exception.CrewErrorCode;
import com.oit.dondok.domain.crew.repository.CrewParticipantRepository;
import com.oit.dondok.domain.crew.repository.CrewRepository;
import com.oit.dondok.domain.image.port.ImageDeliveryPort;
import com.oit.dondok.domain.image.port.ImageObjectKey;
import com.oit.dondok.domain.mission.dto.response.MissionLogItemResponse;
import com.oit.dondok.domain.mission.dto.response.MissionLogListResponse;
import com.oit.dondok.domain.mission.repository.MissionLogItemProjection;
import com.oit.dondok.domain.mission.repository.MissionLogQueryRepository;
import com.oit.dondok.global.exception.CustomException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
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
@Transactional(readOnly = true)
public class MissionLogQueryService {

  private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");
  private static final Duration MISSION_IMAGE_URL_TTL = Duration.ofMinutes(10);
  private static final int DEFAULT_LIMIT = 20;
  private static final int MAX_LIMIT = 100;
  private static final String CURSOR_VERSION = "v1";
  private static final String CURSOR_DELIMITER = "|";

  private final CrewRepository crewRepository;
  private final CrewParticipantRepository crewParticipantRepository;
  private final MissionLogQueryRepository missionLogQueryRepository;
  private final ImageDeliveryPort imageDeliveryPort;

  // 특정 크루에서 현재 사용자가 제출한 미션 인증 로그 목록을 조회한다.
  public MissionLogListResponse findMyMissionLogs(
      UUID memberUuid, Long crewId, Integer limitInput, String cursorInput) {
    // 크루가 없거나 현재 사용자가 해당 크루 참여자가 아니면 명세의 에러를 반환한다.
    validateCrewExists(crewId);
    validateParticipantExists(memberUuid, crewId);

    int effectiveLimit = resolveLimit(limitInput);
    Cursor cursor = parseCursor(cursorInput);

    // 다음 페이지 존재 여부를 판단하기 위해 요청 limit보다 1개 더 조회한다.
    int queryLimit = effectiveLimit + 1;
    List<MissionLogItemProjection> rows =
        missionLogQueryRepository.findMyMissionLogsByCursor(
            memberUuid, crewId, queryLimit, cursor.serverTime(), cursor.missionLogId());

    boolean hasNext = rows.size() > effectiveLimit;
    List<MissionLogItemProjection> pageItems = hasNext ? rows.subList(0, effectiveLimit) : rows;

    List<MissionLogItemResponse> items = pageItems.stream().map(this::toResponse).toList();
    String nextCursor =
        hasNext
            ? toCursor(
                pageItems.get(pageItems.size() - 1).serverTime(),
                pageItems.get(pageItems.size() - 1).missionLogId())
            : null;

    return new MissionLogListResponse(items, nextCursor);
  }

  private void validateCrewExists(Long crewId) {
    if (!crewRepository.existsById(crewId)) {
      throw new CustomException(CrewErrorCode.CREW_NOT_FOUND);
    }
  }

  private void validateParticipantExists(UUID memberUuid, Long crewId) {
    if (!crewParticipantRepository.existsByCrewIdAndMemberUuid(crewId, memberUuid)) {
      throw new CustomException(CrewErrorCode.PARTICIPANT_NOT_FOUND);
    }
  }

  private MissionLogItemResponse toResponse(MissionLogItemProjection row) {
    return new MissionLogItemResponse(
        row.missionLogId(),
        row.crewParticipantId(),
        createImageUrl(row.imageS3Key()),
        row.caption(),
        row.imageHash(),
        toSeoulOffset(row.serverTime()),
        toSeoulOffset(row.exifTakenAt()),
        row.certificationStatus(),
        row.failureReason(),
        row.decisionType(),
        row.rejectReasonCode());
  }

  private String createImageUrl(String imageS3Key) {
    if (imageS3Key == null || imageS3Key.isBlank()) {
      return null;
    }

    // DB에는 S3 key만 저장하므로 조회 시점에 임시 접근 URL을 생성한다.
    return imageDeliveryPort
        .createDeliveryUrl(new ImageObjectKey(imageS3Key), MISSION_IMAGE_URL_TTL)
        .url();
  }

  private int resolveLimit(Integer limitInput) {
    if (limitInput == null) {
      return DEFAULT_LIMIT;
    }

    // 명세에 limit 오류 응답이 없으므로 허용 범위 안으로 보정한다.
    return Math.min(Math.max(limitInput, 1), MAX_LIMIT);
  }

  private Cursor parseCursor(String cursorInput) {
    if (cursorInput == null || cursorInput.isBlank()) {
      return new Cursor(null, null);
    }

    // cursor는 v1|server_time|mission_log_id를 URL-safe Base64로 인코딩한 값이다.
    String paddedCursor = restoreBase64Padding(cursorInput.trim());
    try {
      String decoded =
          new String(Base64.getUrlDecoder().decode(paddedCursor), StandardCharsets.UTF_8);
      String[] parts = decoded.split("\\|", -1);
      if (parts.length != 3 || !CURSOR_VERSION.equals(parts[0])) {
        throw new CustomException(CrewErrorCode.INVALID_CURSOR);
      }

      OffsetDateTime serverTime = OffsetDateTime.parse(parts[1]);
      Long missionLogId = Long.parseLong(parts[2]);
      return new Cursor(serverTime.atZoneSameInstant(SEOUL_ZONE).toLocalDateTime(), missionLogId);
    } catch (DateTimeParseException | IllegalArgumentException exception) {
      throw new CustomException(CrewErrorCode.INVALID_CURSOR);
    }
  }

  private String toCursor(LocalDateTime serverTime, Long missionLogId) {
    // 정렬 기준(server_time desc, id desc)과 같은 값을 cursor에 담는다.
    String payload =
        String.join(
            CURSOR_DELIMITER,
            CURSOR_VERSION,
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
      throw new CustomException(CrewErrorCode.INVALID_CURSOR);
    }
    return cursor + "=".repeat(4 - remainder);
  }

  private static OffsetDateTime toSeoulOffset(LocalDateTime dateTime) {
    if (dateTime == null) {
      return null;
    }
    return dateTime.atZone(SEOUL_ZONE).toOffsetDateTime();
  }

  private record Cursor(LocalDateTime serverTime, Long missionLogId) {}
}
