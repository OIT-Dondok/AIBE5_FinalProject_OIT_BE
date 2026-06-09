package com.oit.dondok.domain.mission.repository;

import com.oit.dondok.domain.mission.entity.CertificationStatus;
import com.oit.dondok.domain.mission.entity.MissionFailureReason;
import com.oit.dondok.domain.mission.entity.ModerationDecisionType;
import com.oit.dondok.domain.mission.entity.RejectReasonCode;
import java.time.LocalDateTime;

public record MissionLogItemProjection(
    Long missionLogId,
    Long crewParticipantId,
    String imageS3Key,
    String caption,
    String imageHash,
    LocalDateTime serverTime,
    LocalDateTime exifTakenAt,
    CertificationStatus certificationStatus,
    MissionFailureReason failureReason,
    ModerationDecisionType decisionType,
    RejectReasonCode rejectReasonCode) {}
