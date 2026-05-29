package com.oit.dondok.domain.mission.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import com.querydsl.core.types.dsl.PathInits;
import javax.annotation.processing.Generated;

/** QMissionLogReaction is a Querydsl query type for MissionLogReaction */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMissionLogReaction extends EntityPathBase<MissionLogReaction> {

  private static final long serialVersionUID = -2031726192L;

  private static final PathInits INITS = PathInits.DIRECT2;

  public static final QMissionLogReaction missionLogReaction =
      new QMissionLogReaction("missionLogReaction");

  public final com.oit.dondok.global.entity.QAuditableTimeEntity _super =
      new com.oit.dondok.global.entity.QAuditableTimeEntity(this);

  // inherited
  public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

  public final NumberPath<Long> id = createNumber("id", Long.class);

  public final com.oit.dondok.domain.member.entity.QMember member;

  public final QMissionLog missionLog;

  public final StringPath reactionType = createString("reactionType");

  // inherited
  public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

  public QMissionLogReaction(String variable) {
    this(MissionLogReaction.class, forVariable(variable), INITS);
  }

  public QMissionLogReaction(Path<? extends MissionLogReaction> path) {
    this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
  }

  public QMissionLogReaction(PathMetadata metadata) {
    this(metadata, PathInits.getFor(metadata, INITS));
  }

  public QMissionLogReaction(PathMetadata metadata, PathInits inits) {
    this(MissionLogReaction.class, metadata, inits);
  }

  public QMissionLogReaction(
      Class<? extends MissionLogReaction> type, PathMetadata metadata, PathInits inits) {
    super(type, metadata, inits);
    this.member =
        inits.isInitialized("member")
            ? new com.oit.dondok.domain.member.entity.QMember(forProperty("member"))
            : null;
    this.missionLog =
        inits.isInitialized("missionLog")
            ? new QMissionLog(forProperty("missionLog"), inits.get("missionLog"))
            : null;
  }
}
