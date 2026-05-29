package com.oit.dondok.domain.mission.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMissionLog is a Querydsl query type for MissionLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMissionLog extends EntityPathBase<MissionLog> {

    private static final long serialVersionUID = 2142259783L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMissionLog missionLog = new QMissionLog("missionLog");

    public final com.oit.dondok.global.entity.QAuditableTimeEntity _super = new com.oit.dondok.global.entity.QAuditableTimeEntity(this);

    public final StringPath caption = createString("caption");

    public final EnumPath<CertificationStatus> certificationStatus = createEnum("certificationStatus", CertificationStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.oit.dondok.domain.crew.entity.QCrewParticipant crewParticipant;

    public final EnumPath<ModerationDecisionType> decisionType = createEnum("decisionType", ModerationDecisionType.class);

    public final DateTimePath<java.time.LocalDateTime> exifTakenAt = createDateTime("exifTakenAt", java.time.LocalDateTime.class);

    public final EnumPath<MissionFailureReason> failureReason = createEnum("failureReason", MissionFailureReason.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imageHash = createString("imageHash");

    public final StringPath imageS3Key = createString("imageS3Key");

    public final StringPath imageUrl = createString("imageUrl");

    public final com.oit.dondok.domain.member.entity.QMember moderator;

    public final DateTimePath<java.time.LocalDateTime> moderatorDecidedAt = createDateTime("moderatorDecidedAt", java.time.LocalDateTime.class);

    public final StringPath rejectMemo = createString("rejectMemo");

    public final EnumPath<RejectReasonCode> rejectReasonCode = createEnum("rejectReasonCode", RejectReasonCode.class);

    public final DateTimePath<java.time.LocalDateTime> serverTime = createDateTime("serverTime", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QMissionLog(String variable) {
        this(MissionLog.class, forVariable(variable), INITS);
    }

    public QMissionLog(Path<? extends MissionLog> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMissionLog(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMissionLog(PathMetadata metadata, PathInits inits) {
        this(MissionLog.class, metadata, inits);
    }

    public QMissionLog(Class<? extends MissionLog> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.crewParticipant = inits.isInitialized("crewParticipant") ? new com.oit.dondok.domain.crew.entity.QCrewParticipant(forProperty("crewParticipant"), inits.get("crewParticipant")) : null;
        this.moderator = inits.isInitialized("moderator") ? new com.oit.dondok.domain.member.entity.QMember(forProperty("moderator")) : null;
    }

}

