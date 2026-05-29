package com.oit.dondok.domain.crew.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCrew is a Querydsl query type for Crew
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCrew extends EntityPathBase<Crew> {

    private static final long serialVersionUID = -1077798951L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCrew crew = new QCrew("crew");

    public final com.oit.dondok.global.entity.QAuditableTimeEntity _super = new com.oit.dondok.global.entity.QAuditableTimeEntity(this);

    public final DateTimePath<java.time.LocalDateTime> activatedAt = createDateTime("activatedAt", java.time.LocalDateTime.class);

    public final StringPath category = createString("category");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> depositAmount = createNumber("depositAmount", Long.class);

    public final StringPath description = createString("description");

    public final DateTimePath<java.time.LocalDateTime> endAt = createDateTime("endAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> hostAgreedAt = createDateTime("hostAgreedAt", java.time.LocalDateTime.class);

    public final StringPath hostAgreementSnapshot = createString("hostAgreementSnapshot");

    public final EnumPath<HostPolicyVersion> hostAgreementVersion = createEnum("hostAgreementVersion", HostPolicyVersion.class);

    public final com.oit.dondok.domain.member.entity.QMember hostMember;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imageS3Key = createString("imageS3Key");

    public final NumberPath<Integer> maxParticipants = createNumber("maxParticipants", Integer.class);

    public final NumberPath<Integer> minParticipants = createNumber("minParticipants", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> recruitmentDeadline = createDateTime("recruitmentDeadline", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> startAt = createDateTime("startAt", java.time.LocalDateTime.class);

    public final EnumPath<CrewStatus> status = createEnum("status", CrewStatus.class);

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QCrew(String variable) {
        this(Crew.class, forVariable(variable), INITS);
    }

    public QCrew(Path<? extends Crew> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCrew(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCrew(PathMetadata metadata, PathInits inits) {
        this(Crew.class, metadata, inits);
    }

    public QCrew(Class<? extends Crew> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.hostMember = inits.isInitialized("hostMember") ? new com.oit.dondok.domain.member.entity.QMember(forProperty("hostMember")) : null;
    }

}

