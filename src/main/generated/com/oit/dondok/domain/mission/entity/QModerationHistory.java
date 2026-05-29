package com.oit.dondok.domain.mission.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QModerationHistory is a Querydsl query type for ModerationHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QModerationHistory extends EntityPathBase<ModerationHistory> {

    private static final long serialVersionUID = -1638022785L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QModerationHistory moderationHistory = new QModerationHistory("moderationHistory");

    public final StringPath afterState = createString("afterState");

    public final StringPath beforeState = createString("beforeState");

    public final DateTimePath<java.time.LocalDateTime> changedAt = createDateTime("changedAt", java.time.LocalDateTime.class);

    public final EnumPath<ModerationDecisionType> decisionType = createEnum("decisionType", ModerationDecisionType.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QMissionLog missionLog;

    public final com.oit.dondok.domain.member.entity.QMember moderator;

    public final StringPath rejectMemo = createString("rejectMemo");

    public final EnumPath<RejectReasonCode> rejectReasonCode = createEnum("rejectReasonCode", RejectReasonCode.class);

    public QModerationHistory(String variable) {
        this(ModerationHistory.class, forVariable(variable), INITS);
    }

    public QModerationHistory(Path<? extends ModerationHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QModerationHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QModerationHistory(PathMetadata metadata, PathInits inits) {
        this(ModerationHistory.class, metadata, inits);
    }

    public QModerationHistory(Class<? extends ModerationHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.missionLog = inits.isInitialized("missionLog") ? new QMissionLog(forProperty("missionLog"), inits.get("missionLog")) : null;
        this.moderator = inits.isInitialized("moderator") ? new com.oit.dondok.domain.member.entity.QMember(forProperty("moderator")) : null;
    }

}

