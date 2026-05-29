package com.oit.dondok.domain.settlement.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSettlementItem is a Querydsl query type for SettlementItem
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSettlementItem extends EntityPathBase<SettlementItem> {

    private static final long serialVersionUID = -2087945572L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSettlementItem settlementItem = new QSettlementItem("settlementItem");

    public final com.oit.dondok.global.entity.QAuditableTimeEntity _super = new com.oit.dondok.global.entity.QAuditableTimeEntity(this);

    public final NumberPath<Long> baseRefundAmount = createNumber("baseRefundAmount", Long.class);

    public final StringPath calculationReason = createString("calculationReason");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.oit.dondok.domain.crew.entity.QCrewParticipant crewParticipant;

    public final NumberPath<Long> depositAmount = createNumber("depositAmount", Long.class);

    public final StringPath effectiveModerationSnapshot = createString("effectiveModerationSnapshot");

    public final NumberPath<Integer> excludedSuccessCount = createNumber("excludedSuccessCount", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.oit.dondok.domain.member.entity.QMember member;

    public final StringPath moderationChainRef = createString("moderationChainRef");

    public final EnumPath<ParticipantStatusSnapshot> participantStatusSnapshot = createEnum("participantStatusSnapshot", ParticipantStatusSnapshot.class);

    public final DateTimePath<java.time.LocalDateTime> periodEndAt = createDateTime("periodEndAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> periodStartAt = createDateTime("periodStartAt", java.time.LocalDateTime.class);

    public final com.oit.dondok.domain.point.entity.QPointHistory pointHistory;

    public final NumberPath<Integer> recognizedDatesCount = createNumber("recognizedDatesCount", Integer.class);

    public final NumberPath<Integer> recognizedSuccessCount = createNumber("recognizedSuccessCount", Integer.class);

    public final NumberPath<Long> refundAmount = createNumber("refundAmount", Long.class);

    public final NumberPath<Long> remainderBonusAmount = createNumber("remainderBonusAmount", Long.class);

    public final QSettlement settlement;

    public final NumberPath<java.math.BigDecimal> shareRatio = createNumber("shareRatio", java.math.BigDecimal.class);

    public final NumberPath<Integer> successCountRaw = createNumber("successCountRaw", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QSettlementItem(String variable) {
        this(SettlementItem.class, forVariable(variable), INITS);
    }

    public QSettlementItem(Path<? extends SettlementItem> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSettlementItem(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSettlementItem(PathMetadata metadata, PathInits inits) {
        this(SettlementItem.class, metadata, inits);
    }

    public QSettlementItem(Class<? extends SettlementItem> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.crewParticipant = inits.isInitialized("crewParticipant") ? new com.oit.dondok.domain.crew.entity.QCrewParticipant(forProperty("crewParticipant"), inits.get("crewParticipant")) : null;
        this.member = inits.isInitialized("member") ? new com.oit.dondok.domain.member.entity.QMember(forProperty("member")) : null;
        this.pointHistory = inits.isInitialized("pointHistory") ? new com.oit.dondok.domain.point.entity.QPointHistory(forProperty("pointHistory"), inits.get("pointHistory")) : null;
        this.settlement = inits.isInitialized("settlement") ? new QSettlement(forProperty("settlement"), inits.get("settlement")) : null;
    }

}

