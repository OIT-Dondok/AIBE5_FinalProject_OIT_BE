package com.oit.dondok.domain.point.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import com.querydsl.core.types.dsl.PathInits;
import javax.annotation.processing.Generated;

/** QPointAccount is a Querydsl query type for PointAccount */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPointAccount extends EntityPathBase<PointAccount> {

  private static final long serialVersionUID = -2116515216L;

  private static final PathInits INITS = PathInits.DIRECT2;

  public static final QPointAccount pointAccount = new QPointAccount("pointAccount");

  public final com.oit.dondok.global.entity.QAuditableTimeEntity _super =
      new com.oit.dondok.global.entity.QAuditableTimeEntity(this);

  public final NumberPath<Long> availableBalance = createNumber("availableBalance", Long.class);

  // inherited
  public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

  public final NumberPath<Long> id = createNumber("id", Long.class);

  public final NumberPath<Long> lockedBalance = createNumber("lockedBalance", Long.class);

  public final com.oit.dondok.domain.member.entity.QMember member;

  public final NumberPath<Long> reservedBalance = createNumber("reservedBalance", Long.class);

  // inherited
  public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

  public final NumberPath<Long> version = createNumber("version", Long.class);

  public QPointAccount(String variable) {
    this(PointAccount.class, forVariable(variable), INITS);
  }

  public QPointAccount(Path<? extends PointAccount> path) {
    this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
  }

  public QPointAccount(PathMetadata metadata) {
    this(metadata, PathInits.getFor(metadata, INITS));
  }

  public QPointAccount(PathMetadata metadata, PathInits inits) {
    this(PointAccount.class, metadata, inits);
  }

  public QPointAccount(Class<? extends PointAccount> type, PathMetadata metadata, PathInits inits) {
    super(type, metadata, inits);
    this.member =
        inits.isInitialized("member")
            ? new com.oit.dondok.domain.member.entity.QMember(forProperty("member"))
            : null;
  }
}
