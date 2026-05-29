package com.oit.dondok.domain.point.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPointHistory is a Querydsl query type for PointHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPointHistory extends EntityPathBase<PointHistory> {

    private static final long serialVersionUID = -12262185L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPointHistory pointHistory = new QPointHistory("pointHistory");

    public final com.oit.dondok.global.entity.QCreatedTimeEntity _super = new com.oit.dondok.global.entity.QCreatedTimeEntity(this);

    public final NumberPath<Long> amount = createNumber("amount", Long.class);

    public final NumberPath<Long> availableAfter = createNumber("availableAfter", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath idempotencyKey = createString("idempotencyKey");

    public final NumberPath<Long> lockedAfter = createNumber("lockedAfter", Long.class);

    public final com.oit.dondok.domain.member.entity.QMember member;

    public final NumberPath<Long> referenceId = createNumber("referenceId", Long.class);

    public final EnumPath<PointReferenceType> referenceType = createEnum("referenceType", PointReferenceType.class);

    public final NumberPath<Long> reservedAfter = createNumber("reservedAfter", Long.class);

    public final EnumPath<PointTransactionType> transactionType = createEnum("transactionType", PointTransactionType.class);

    public QPointHistory(String variable) {
        this(PointHistory.class, forVariable(variable), INITS);
    }

    public QPointHistory(Path<? extends PointHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPointHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPointHistory(PathMetadata metadata, PathInits inits) {
        this(PointHistory.class, metadata, inits);
    }

    public QPointHistory(Class<? extends PointHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new com.oit.dondok.domain.member.entity.QMember(forProperty("member")) : null;
    }

}

