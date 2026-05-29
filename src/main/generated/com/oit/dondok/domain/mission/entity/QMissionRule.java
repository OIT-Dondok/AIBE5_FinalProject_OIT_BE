package com.oit.dondok.domain.mission.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMissionRule is a Querydsl query type for MissionRule
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMissionRule extends EntityPathBase<MissionRule> {

    private static final long serialVersionUID = 1985728601L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMissionRule missionRule = new QMissionRule("missionRule");

    public final com.oit.dondok.global.entity.QAuditableTimeEntity _super = new com.oit.dondok.global.entity.QAuditableTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.oit.dondok.domain.crew.entity.QCrew crew;

    public final EnumPath<DailySettlementType> dailySettlementType = createEnum("dailySettlementType", DailySettlementType.class);

    public final EnumPath<MissionFrequencyType> frequencyType = createEnum("frequencyType", MissionFrequencyType.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QMissionRule(String variable) {
        this(MissionRule.class, forVariable(variable), INITS);
    }

    public QMissionRule(Path<? extends MissionRule> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMissionRule(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMissionRule(PathMetadata metadata, PathInits inits) {
        this(MissionRule.class, metadata, inits);
    }

    public QMissionRule(Class<? extends MissionRule> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.crew = inits.isInitialized("crew") ? new com.oit.dondok.domain.crew.entity.QCrew(forProperty("crew"), inits.get("crew")) : null;
    }

}

