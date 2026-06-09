package com.oit.dondok.domain.mission.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.oit.dondok.domain.mission.entity.CertificationStatus;
import com.oit.dondok.domain.mission.entity.ExifRisk;
import com.oit.dondok.domain.mission.entity.ModerationDecisionType;
import com.oit.dondok.domain.mission.entity.RejectReasonCode;
import java.time.OffsetDateTime;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record MissionReviewSummaryItemResponse(
    Long missionLogId,
    Long crewId,
    Long crewParticipantId,
    UUID submitterMemberUuid,
    String submitterNickname,
    String imageUrl,
    String caption,
    OffsetDateTime serverTime,
    OffsetDateTime exifTakenAt,
    ExifRisk exifRisk,
    boolean duplicateHash,
    CertificationStatus certificationStatus,
    ModerationDecisionType decisionType,
    RejectReasonCode rejectReasonCode,
    MissionReviewCategory reviewCategory,
    OffsetDateTime reviewDeadlineAt) {}
