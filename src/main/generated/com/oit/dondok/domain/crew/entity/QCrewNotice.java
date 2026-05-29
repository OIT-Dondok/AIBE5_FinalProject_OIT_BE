package com.oit.dondok.domain.crew.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCrewNotice is a Querydsl query type for CrewNotice
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCrewNotice extends EntityPathBase<CrewNotice> {

    private static final long serialVersionUID = 2015990801L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCrewNotice crewNotice = new QCrewNotice("crewNotice");

    public final com.oit.dondok.global.entity.QAuditableTimeEntity _super = new com.oit.dondok.global.entity.QAuditableTimeEntity(this);

    public final com.oit.dondok.domain.member.entity.QMember authorMember;

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QCrew crew;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<CrewNoticeStatus> status = createEnum("status", CrewNoticeStatus.class);

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QCrewNotice(String variable) {
        this(CrewNotice.class, forVariable(variable), INITS);
    }

    public QCrewNotice(Path<? extends CrewNotice> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCrewNotice(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCrewNotice(PathMetadata metadata, PathInits inits) {
        this(CrewNotice.class, metadata, inits);
    }

    public QCrewNotice(Class<? extends CrewNotice> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.authorMember = inits.isInitialized("authorMember") ? new com.oit.dondok.domain.member.entity.QMember(forProperty("authorMember")) : null;
        this.crew = inits.isInitialized("crew") ? new QCrew(forProperty("crew"), inits.get("crew")) : null;
    }

}

