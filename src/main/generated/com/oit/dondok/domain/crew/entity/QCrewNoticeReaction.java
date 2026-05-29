package com.oit.dondok.domain.crew.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCrewNoticeReaction is a Querydsl query type for CrewNoticeReaction
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCrewNoticeReaction extends EntityPathBase<CrewNoticeReaction> {

    private static final long serialVersionUID = 2013671770L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCrewNoticeReaction crewNoticeReaction = new QCrewNoticeReaction("crewNoticeReaction");

    public final com.oit.dondok.global.entity.QAuditableTimeEntity _super = new com.oit.dondok.global.entity.QAuditableTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QCrewNotice crewNotice;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.oit.dondok.domain.member.entity.QMember member;

    public final StringPath reactionType = createString("reactionType");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QCrewNoticeReaction(String variable) {
        this(CrewNoticeReaction.class, forVariable(variable), INITS);
    }

    public QCrewNoticeReaction(Path<? extends CrewNoticeReaction> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCrewNoticeReaction(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCrewNoticeReaction(PathMetadata metadata, PathInits inits) {
        this(CrewNoticeReaction.class, metadata, inits);
    }

    public QCrewNoticeReaction(Class<? extends CrewNoticeReaction> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.crewNotice = inits.isInitialized("crewNotice") ? new QCrewNotice(forProperty("crewNotice"), inits.get("crewNotice")) : null;
        this.member = inits.isInitialized("member") ? new com.oit.dondok.domain.member.entity.QMember(forProperty("member")) : null;
    }

}

