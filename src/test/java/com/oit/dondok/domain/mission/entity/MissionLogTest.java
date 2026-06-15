package com.oit.dondok.domain.mission.entity;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.oit.dondok.domain.crew.entity.Crew;
import com.oit.dondok.domain.crew.entity.CrewParticipant;
import com.oit.dondok.domain.crew.entity.HostPolicyVersion;
import com.oit.dondok.domain.member.entity.Member;
import com.oit.dondok.domain.mission.exception.MissionErrorCode;
import com.oit.dondok.global.exception.CustomException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class MissionLogTest {

  @Test
  void rejectAutomaticallyWithoutImageRiskSignal() {
    LocalDateTime now = LocalDateTime.of(2026, 6, 10, 12, 0);
    Member host = Member.create("host@example.com", "password-hash", "host");
    Crew crew =
        Crew.create(
            host,
            "morning crew",
            "daily mission crew",
            null,
            "HEALTH",
            "{}",
            HostPolicyVersion.HOST_POLICY_V1,
            now,
            10_000L,
            2,
            10,
            now.plusDays(1),
            now.plusDays(2),
            now.plusDays(30));
    CrewParticipant participant = CrewParticipant.create(crew, host, 10_000L, now);
    MissionLog missionLog =
        MissionLog.createPendingReview(
            participant,
            "mission/1/1/image.jpg",
            "daily mission done",
            "9b74c9897bac770ffc029102a200c5de8c0e9e5b9d3c9c7e5f4f5c1a2b3c4d5e",
            now,
            ExifRisk.NORMAL,
            false,
            now);

    assertThatThrownBy(() -> missionLog.rejectAutomatically(host, now))
        .isInstanceOf(CustomException.class)
        .extracting("errorCode")
        .isEqualTo(MissionErrorCode.INVALID_AUTO_REJECTION_SIGNAL);
  }
}
