package com.oit.dondok.domain.settlement.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import com.querydsl.core.types.dsl.PathInits;
import javax.annotation.processing.Generated;

/** QSettlement is a Querydsl query type for Settlement */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSettlement extends EntityPathBase<Settlement> {

  private static final long serialVersionUID = 230795241L;

  private static final PathInits INITS = PathInits.DIRECT2;

  public static final QSettlement settlement = new QSettlement("settlement");

  public final com.oit.dondok.global.entity.QAuditableTimeEntity _super =
      new com.oit.dondok.global.entity.QAuditableTimeEntity(this);

  public final StringPath algorithmVersion = createString("algorithmVersion");

  public final DateTimePath<java.time.LocalDateTime> baselineFrozenAt =
      createDateTime("baselineFrozenAt", java.time.LocalDateTime.class);

  public final StringPath batchRunKey = createString("batchRunKey");

  // inherited
  public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

  public final com.oit.dondok.domain.crew.entity.QCrew crew;

  public final EnumPath<SettlementFailureCode> failureCode =
      createEnum("failureCode", SettlementFailureCode.class);

  public final StringPath failureMessage = createString("failureMessage");

  public final DateTimePath<java.time.LocalDateTime> finishedAt =
      createDateTime("finishedAt", java.time.LocalDateTime.class);

  public final NumberPath<Long> id = createNumber("id", Long.class);

  public final EnumPath<RemainderPolicy> remainderPolicy =
      createEnum("remainderPolicy", RemainderPolicy.class);

  public final NumberPath<Integer> retryCount = createNumber("retryCount", Integer.class);

  public final StringPath ruleContextSnapshot = createString("ruleContextSnapshot");

  public final DateTimePath<java.time.LocalDateTime> startedAt =
      createDateTime("startedAt", java.time.LocalDateTime.class);

  public final EnumPath<SettlementStatus> status = createEnum("status", SettlementStatus.class);

  public final NumberPath<Long> totalBaseRefundAmount =
      createNumber("totalBaseRefundAmount", Long.class);

  public final NumberPath<Long> totalLockedAmount = createNumber("totalLockedAmount", Long.class);

  public final NumberPath<Integer> totalParticipants =
      createNumber("totalParticipants", Integer.class);

  public final NumberPath<Integer> totalRecognizedSuccess =
      createNumber("totalRecognizedSuccess", Integer.class);

  public final NumberPath<Long> totalRemainderAmount =
      createNumber("totalRemainderAmount", Long.class);

  // inherited
  public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

  public final NumberPath<Long> version = createNumber("version", Long.class);

  public QSettlement(String variable) {
    this(Settlement.class, forVariable(variable), INITS);
  }

  public QSettlement(Path<? extends Settlement> path) {
    this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
  }

  public QSettlement(PathMetadata metadata) {
    this(metadata, PathInits.getFor(metadata, INITS));
  }

  public QSettlement(PathMetadata metadata, PathInits inits) {
    this(Settlement.class, metadata, inits);
  }

  public QSettlement(Class<? extends Settlement> type, PathMetadata metadata, PathInits inits) {
    super(type, metadata, inits);
    this.crew =
        inits.isInitialized("crew")
            ? new com.oit.dondok.domain.crew.entity.QCrew(forProperty("crew"), inits.get("crew"))
            : null;
  }
}
