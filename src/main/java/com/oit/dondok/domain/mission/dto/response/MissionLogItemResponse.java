package com.oit.dondok.domain.mission.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.oit.dondok.domain.mission.entity.CertificationStatus;
import com.oit.dondok.domain.mission.entity.MissionFailureReason;
import com.oit.dondok.domain.mission.entity.ModerationDecisionType;
import com.oit.dondok.domain.mission.entity.RejectReasonCode;
import java.time.OffsetDateTime;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record MissionLogItemResponse(
    Long missionLogId,
    Long crewParticipantId,
    String imageUrl,
    String caption,
    String imageHash,
    OffsetDateTime serverTime,
    OffsetDateTime exifTakenAt,
    CertificationStatus certificationStatus,
    MissionFailureReason failureReason,
    ModerationDecisionType decisionType,
    RejectReasonCode rejectReasonCode) {}
