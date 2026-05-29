package com.oit.dondok.domain.mission.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import com.querydsl.core.types.dsl.PathInits;
import javax.annotation.processing.Generated;

/** QMissionScheduleDay is a Querydsl query type for MissionScheduleDay */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMissionScheduleDay extends EntityPathBase<MissionScheduleDay> {

  private static final long serialVersionUID = 1566757256L;

  private static final PathInits INITS = PathInits.DIRECT2;

  public static final QMissionScheduleDay missionScheduleDay =
      new QMissionScheduleDay("missionScheduleDay");

  public final com.oit.dondok.global.entity.QCreatedTimeEntity _super =
      new com.oit.dondok.global.entity.QCreatedTimeEntity(this);

  // inherited
  public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

  public final NumberPath<Integer> dayOfWeek = createNumber("dayOfWeek", Integer.class);

  public final NumberPath<Long> id = createNumber("id", Long.class);

  public final QMissionRule missionRule;

  public QMissionScheduleDay(String variable) {
    this(MissionScheduleDay.class, forVariable(variable), INITS);
  }

  public QMissionScheduleDay(Path<? extends MissionScheduleDay> path) {
    this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
  }

  public QMissionScheduleDay(PathMetadata metadata) {
    this(metadata, PathInits.getFor(metadata, INITS));
  }

  public QMissionScheduleDay(PathMetadata metadata, PathInits inits) {
    this(MissionScheduleDay.class, metadata, inits);
  }

  public QMissionScheduleDay(
      Class<? extends MissionScheduleDay> type, PathMetadata metadata, PathInits inits) {
    super(type, metadata, inits);
    this.missionRule =
        inits.isInitialized("missionRule")
            ? new QMissionRule(forProperty("missionRule"), inits.get("missionRule"))
            : null;
  }
}
