package com.oit.dondok.domain.auth.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMemberRefreshToken is a Querydsl query type for MemberRefreshToken
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMemberRefreshToken extends EntityPathBase<MemberRefreshToken> {

    private static final long serialVersionUID = -1127676521L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMemberRefreshToken memberRefreshToken = new QMemberRefreshToken("memberRefreshToken");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> expiresAt = createDateTime("expiresAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.oit.dondok.domain.member.entity.QMember member;

    public final DateTimePath<java.time.LocalDateTime> revokedAt = createDateTime("revokedAt", java.time.LocalDateTime.class);

    public final StringPath tokenHash = createString("tokenHash");

    public QMemberRefreshToken(String variable) {
        this(MemberRefreshToken.class, forVariable(variable), INITS);
    }

    public QMemberRefreshToken(Path<? extends MemberRefreshToken> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMemberRefreshToken(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMemberRefreshToken(PathMetadata metadata, PathInits inits) {
        this(MemberRefreshToken.class, metadata, inits);
    }

    public QMemberRefreshToken(Class<? extends MemberRefreshToken> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new com.oit.dondok.domain.member.entity.QMember(forProperty("member")) : null;
    }

}

