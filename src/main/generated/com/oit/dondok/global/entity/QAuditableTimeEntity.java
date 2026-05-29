package com.oit.dondok.global.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import javax.annotation.processing.Generated;

/** QAuditableTimeEntity is a Querydsl query type for AuditableTimeEntity */
@Generated("com.querydsl.codegen.DefaultSupertypeSerializer")
public class QAuditableTimeEntity extends EntityPathBase<AuditableTimeEntity> {

  private static final long serialVersionUID = 578876789L;

  public static final QAuditableTimeEntity auditableTimeEntity =
      new QAuditableTimeEntity("auditableTimeEntity");

  public final QCreatedTimeEntity _super = new QCreatedTimeEntity(this);

  // inherited
  public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

  public final DateTimePath<java.time.LocalDateTime> updatedAt =
      createDateTime("updatedAt", java.time.LocalDateTime.class);

  public QAuditableTimeEntity(String variable) {
    super(AuditableTimeEntity.class, forVariable(variable));
  }

  public QAuditableTimeEntity(Path<? extends AuditableTimeEntity> path) {
    super(path.getType(), path.getMetadata());
  }

  public QAuditableTimeEntity(PathMetadata metadata) {
    super(AuditableTimeEntity.class, metadata);
  }
}
