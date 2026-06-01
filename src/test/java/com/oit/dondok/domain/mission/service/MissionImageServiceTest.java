package com.oit.dondok.domain.mission.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.oit.dondok.domain.crew.entity.Crew;
import com.oit.dondok.domain.crew.entity.CrewParticipant;
import com.oit.dondok.domain.crew.exception.CrewErrorCode;
import com.oit.dondok.domain.crew.repository.CrewParticipantRepository;
import com.oit.dondok.domain.member.entity.Member;
import com.oit.dondok.global.exception.CustomException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MissionImageServiceTest {

  private static final UUID MEMBER_UUID = UUID.fromString("018f4fd2-6d7a-7a41-9f58-6d07f5c3c901");
  private static final Long CREW_ID = 42L;
  private static final Long PARTICIPANT_ID = 101L;

  @Mock private CrewParticipantRepository crewParticipantRepository;

  @InjectMocks private MissionImageService missionImageService;

  @Test
  void getOwnedParticipantReturnsWhenMemberOwnsParticipantInCrew() {
    CrewParticipant participant = participantOf(MEMBER_UUID, CREW_ID);
    given(crewParticipantRepository.findById(PARTICIPANT_ID)).willReturn(Optional.of(participant));

    CrewParticipant result =
        missionImageService.getOwnedParticipant(MEMBER_UUID, CREW_ID, PARTICIPANT_ID);

    assertThat(result).isSameAs(participant);
  }

  @Test
  void getOwnedParticipantThrowsWhenParticipantNotFound() {
    given(crewParticipantRepository.findById(PARTICIPANT_ID)).willReturn(Optional.empty());

    assertThatThrownBy(
            () -> missionImageService.getOwnedParticipant(MEMBER_UUID, CREW_ID, PARTICIPANT_ID))
        .isInstanceOf(CustomException.class)
        .extracting("errorCode")
        .isEqualTo(CrewErrorCode.PARTICIPANT_NOT_FOUND);
  }

  @Test
  void getOwnedParticipantThrowsWhenRequestedByDifferentMember() {
    CrewParticipant participant = participantOf(UUID.randomUUID(), CREW_ID);
    given(crewParticipantRepository.findById(PARTICIPANT_ID)).willReturn(Optional.of(participant));

    assertThatThrownBy(
            () -> missionImageService.getOwnedParticipant(MEMBER_UUID, CREW_ID, PARTICIPANT_ID))
        .isInstanceOf(CustomException.class)
        .extracting("errorCode")
        .isEqualTo(CrewErrorCode.PARTICIPANT_NOT_FOUND);
  }

  @Test
  void getOwnedParticipantThrowsWhenParticipantBelongsToDifferentCrew() {
    CrewParticipant participant = participantOf(MEMBER_UUID, 999L);
    given(crewParticipantRepository.findById(PARTICIPANT_ID)).willReturn(Optional.of(participant));

    assertThatThrownBy(
            () -> missionImageService.getOwnedParticipant(MEMBER_UUID, CREW_ID, PARTICIPANT_ID))
        .isInstanceOf(CustomException.class)
        .extracting("errorCode")
        .isEqualTo(CrewErrorCode.PARTICIPANT_NOT_FOUND);
  }

  private static CrewParticipant participantOf(UUID memberUuid, Long crewId) {
    CrewParticipant participant = mock(CrewParticipant.class);
    Member member = mock(Member.class);
    Crew crew = mock(Crew.class);
    given(participant.getMember()).willReturn(member);
    given(member.getUuid()).willReturn(memberUuid);
    given(participant.getCrew()).willReturn(crew);
    given(crew.getId()).willReturn(crewId);
    return participant;
  }
}
