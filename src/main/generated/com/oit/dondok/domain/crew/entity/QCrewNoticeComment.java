package com.oit.dondok.domain.crew.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCrewNoticeComment is a Querydsl query type for CrewNoticeComment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCrewNoticeComment extends EntityPathBase<CrewNoticeComment> {

    private static final long serialVersionUID = 1736076558L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCrewNoticeComment crewNoticeComment = new QCrewNoticeComment("crewNoticeComment");

    public final com.oit.dondok.global.entity.QAuditableTimeEntity _super = new com.oit.dondok.global.entity.QAuditableTimeEntity(this);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QCrewNotice crewNotice;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.oit.dondok.domain.member.entity.QMember member;

    public final EnumPath<CrewNoticeCommentStatus> status = createEnum("status", CrewNoticeCommentStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QCrewNoticeComment(String variable) {
        this(CrewNoticeComment.class, forVariable(variable), INITS);
    }

    public QCrewNoticeComment(Path<? extends CrewNoticeComment> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCrewNoticeComment(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCrewNoticeComment(PathMetadata metadata, PathInits inits) {
        this(CrewNoticeComment.class, metadata, inits);
    }

    public QCrewNoticeComment(Class<? extends CrewNoticeComment> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.crewNotice = inits.isInitialized("crewNotice") ? new QCrewNotice(forProperty("crewNotice"), inits.get("crewNotice")) : null;
        this.member = inits.isInitialized("member") ? new com.oit.dondok.domain.member.entity.QMember(forProperty("member")) : null;
    }

}

