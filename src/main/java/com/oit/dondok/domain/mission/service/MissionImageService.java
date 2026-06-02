package com.oit.dondok.domain.mission.service;

import com.oit.dondok.domain.crew.entity.CrewParticipant;
import com.oit.dondok.domain.crew.exception.CrewErrorCode;
import com.oit.dondok.domain.crew.repository.CrewParticipantRepository;
import com.oit.dondok.domain.mission.dto.response.ImageVerifyResult;
import com.oit.dondok.domain.mission.entity.ExifRisk;
import com.oit.dondok.domain.mission.port.ImageMetadata;
import com.oit.dondok.domain.mission.port.ImageMetadataPort;
import com.oit.dondok.domain.mission.repository.MissionLogRepository;
import com.oit.dondok.global.exception.CustomException;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MissionImageService {

  private final CrewParticipantRepository crewParticipantRepository;
  private final ImageMetadataPort imageMetadataPort;
  private final MissionLogRepository missionLogRepository;

  // 미션 이미지 업로드 소유권 검증
  @Transactional(readOnly = true)
  public CrewParticipant getOwnedParticipant(UUID memberUuid, Long crewId, Long crewParticipantId) {
    CrewParticipant participant =
        crewParticipantRepository
            .findById(crewParticipantId)
            .orElseThrow(() -> new CustomException(CrewErrorCode.PARTICIPANT_NOT_FOUND));

    boolean ownedByMember = participant.getMember().getUuid().equals(memberUuid);
    boolean belongsToCrew = participant.getCrew().getId().equals(crewId);
    if (!ownedByMember || !belongsToCrew) {
      throw new CustomException(CrewErrorCode.PARTICIPANT_NOT_FOUND);
    }

    return participant;
  }

  // 원본 이미지에서 EXIF 시각, 해시를 추출하고 risk signal(ExifRisk, 중복)을 산출
  @Transactional(readOnly = true)
  public ImageVerifyResult getImageVerifyResult(
      Long crewId, String s3Key, LocalDateTime serverTime) {
    ImageMetadata metadata = imageMetadataPort.extract(s3Key);

    ExifRisk exifRisk = classifyExifRisk(metadata.takenAt(), serverTime);
    boolean duplicate = missionLogRepository.existsByCrewParticipant_Crew_IdAndImageHash(crewId, metadata.sha256());
    return new ImageVerifyResult(metadata.takenAt(), metadata.sha256(), exifRisk, duplicate);
  }

  private ExifRisk classifyExifRisk(LocalDateTime takenAt, LocalDateTime serverTime) {
    if (takenAt == null) {
      return ExifRisk.MISSING;
    }
    // 인증 대상 날짜는 server_time(Asia/Seoul) 기준. 윈도우 = [당일 00:00, server_time]
    LocalDateTime windowStart = serverTime.toLocalDate().atStartOfDay();
    // TODO: 윈도우 끝을 DailySettlementType 인증 마감으로 좁힌다.
    if (takenAt.isBefore(windowStart) || takenAt.isAfter(serverTime)) {
      return ExifRisk.TIME_INVALID;
    }
    return ExifRisk.NORMAL;
  }
}
