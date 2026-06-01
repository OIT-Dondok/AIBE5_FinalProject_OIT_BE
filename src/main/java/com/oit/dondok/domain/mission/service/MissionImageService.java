package com.oit.dondok.domain.mission.service;

import com.oit.dondok.domain.crew.entity.CrewParticipant;
import com.oit.dondok.domain.crew.exception.CrewErrorCode;
import com.oit.dondok.domain.crew.repository.CrewParticipantRepository;
import com.oit.dondok.global.exception.CustomException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MissionImageService {

  private final CrewParticipantRepository crewParticipantRepository;

  // 미션 이미지 업로드 대상 participant가 요청자(memberUuid) 소유이고 해당 crew에 속하는지 검증한다.
  // 위반/부재는 존재를 숨기기 위해 PARTICIPANT_NOT_FOUND(404)로 통일한다.
  // (LOCKED 상태, 미션 가능일, 인증 중복 등 추가 pre-check는 mission-log 검증 플로우에서 확장한다.)
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
}
