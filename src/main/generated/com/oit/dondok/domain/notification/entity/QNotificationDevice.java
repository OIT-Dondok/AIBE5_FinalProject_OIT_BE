package com.oit.dondok.domain.notification.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import com.querydsl.core.types.dsl.PathInits;
import javax.annotation.processing.Generated;

/** QNotificationDevice is a Querydsl query type for NotificationDevice */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNotificationDevice extends EntityPathBase<NotificationDevice> {

  private static final long serialVersionUID = -1822007133L;

  private static final PathInits INITS = PathInits.DIRECT2;

  public static final QNotificationDevice notificationDevice =
      new QNotificationDevice("notificationDevice");

  public final com.oit.dondok.global.entity.QAuditableTimeEntity _super =
      new com.oit.dondok.global.entity.QAuditableTimeEntity(this);

  public final StringPath appVersion = createString("appVersion");

  // inherited
  public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

  public final StringPath deviceId = createString("deviceId");

  public final BooleanPath enabled = createBoolean("enabled");

  public final StringPath fcmToken = createString("fcmToken");

  public final NumberPath<Long> id = createNumber("id", Long.class);

  public final com.oit.dondok.domain.member.entity.QMember member;

  public final EnumPath<NotificationPlatform> platform =
      createEnum("platform", NotificationPlatform.class);

  // inherited
  public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

  public QNotificationDevice(String variable) {
    this(NotificationDevice.class, forVariable(variable), INITS);
  }

  public QNotificationDevice(Path<? extends NotificationDevice> path) {
    this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
  }

  public QNotificationDevice(PathMetadata metadata) {
    this(metadata, PathInits.getFor(metadata, INITS));
  }

  public QNotificationDevice(PathMetadata metadata, PathInits inits) {
    this(NotificationDevice.class, metadata, inits);
  }

  public QNotificationDevice(
      Class<? extends NotificationDevice> type, PathMetadata metadata, PathInits inits) {
    super(type, metadata, inits);
    this.member =
        inits.isInitialized("member")
            ? new com.oit.dondok.domain.member.entity.QMember(forProperty("member"))
            : null;
  }
}
