package com.oit.dondok.domain.notification.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import com.querydsl.core.types.dsl.PathInits;
import javax.annotation.processing.Generated;

/** QNotification is a Querydsl query type for Notification */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNotification extends EntityPathBase<Notification> {

  private static final long serialVersionUID = -974537747L;

  private static final PathInits INITS = PathInits.DIRECT2;

  public static final QNotification notification = new QNotification("notification");

  public final com.oit.dondok.global.entity.QAuditableTimeEntity _super =
      new com.oit.dondok.global.entity.QAuditableTimeEntity(this);

  // inherited
  public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

  public final StringPath deepLink = createString("deepLink");

  public final StringPath displayText = createString("displayText");

  public final StringPath eventType = createString("eventType");

  public final NumberPath<Long> id = createNumber("id", Long.class);

  public final com.oit.dondok.domain.member.entity.QMember member;

  public final DateTimePath<java.time.LocalDateTime> occurredAt =
      createDateTime("occurredAt", java.time.LocalDateTime.class);

  public final DateTimePath<java.time.LocalDateTime> readAt =
      createDateTime("readAt", java.time.LocalDateTime.class);

  public final BooleanPath requiresRefetch = createBoolean("requiresRefetch");

  public final StringPath resourceId = createString("resourceId");

  public final StringPath resourceType = createString("resourceType");

  // inherited
  public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

  public final ComparablePath<java.util.UUID> uuid = createComparable("uuid", java.util.UUID.class);

  public QNotification(String variable) {
    this(Notification.class, forVariable(variable), INITS);
  }

  public QNotification(Path<? extends Notification> path) {
    this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
  }

  public QNotification(PathMetadata metadata) {
    this(metadata, PathInits.getFor(metadata, INITS));
  }

  public QNotification(PathMetadata metadata, PathInits inits) {
    this(Notification.class, metadata, inits);
  }

  public QNotification(Class<? extends Notification> type, PathMetadata metadata, PathInits inits) {
    super(type, metadata, inits);
    this.member =
        inits.isInitialized("member")
            ? new com.oit.dondok.domain.member.entity.QMember(forProperty("member"))
            : null;
  }
}
