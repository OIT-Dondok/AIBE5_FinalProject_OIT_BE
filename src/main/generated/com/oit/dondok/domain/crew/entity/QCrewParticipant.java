package com.oit.dondok.domain.crew.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCrewParticipant is a Querydsl query type for CrewParticipant
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCrewParticipant extends EntityPathBase<CrewParticipant> {

    private static final long serialVersionUID = 1066814874L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCrewParticipant crewParticipant = new QCrewParticipant("crewParticipant");

    public final com.oit.dondok.global.entity.QAuditableTimeEntity _super = new com.oit.dondok.global.entity.QAuditableTimeEntity(this);

    public final DateTimePath<java.time.LocalDateTime> cancelledAt = createDateTime("cancelledAt", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QCrew crew;

    public final NumberPath<Long> depositAmount = createNumber("depositAmount", Long.class);

    public final DateTimePath<java.time.LocalDateTime> expiredAt = createDateTime("expiredAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> lockedAt = createDateTime("lockedAt", java.time.LocalDateTime.class);

    public final com.oit.dondok.domain.member.entity.QMember member;

    public final DateTimePath<java.time.LocalDateTime> pendingAt = createDateTime("pendingAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> rejectedAt = createDateTime("rejectedAt", java.time.LocalDateTime.class);

    public final com.oit.dondok.domain.point.entity.QPointHistory releasedPointHistory;

    public final EnumPath<CrewParticipantStatus> status = createEnum("status", CrewParticipantStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public QCrewParticipant(String variable) {
        this(CrewParticipant.class, forVariable(variable), INITS);
    }

    public QCrewParticipant(Path<? extends CrewParticipant> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCrewParticipant(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCrewParticipant(PathMetadata metadata, PathInits inits) {
        this(CrewParticipant.class, metadata, inits);
    }

    public QCrewParticipant(Class<? extends CrewParticipant> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.crew = inits.isInitialized("crew") ? new QCrew(forProperty("crew"), inits.get("crew")) : null;
        this.member = inits.isInitialized("member") ? new com.oit.dondok.domain.member.entity.QMember(forProperty("member")) : null;
        this.releasedPointHistory = inits.isInitialized("releasedPointHistory") ? new com.oit.dondok.domain.point.entity.QPointHistory(forProperty("releasedPointHistory"), inits.get("releasedPointHistory")) : null;
    }

}

