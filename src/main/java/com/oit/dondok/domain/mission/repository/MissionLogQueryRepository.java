package com.oit.dondok.domain.mission.repository;

import static com.oit.dondok.domain.crew.entity.QCrew.crew;
import static com.oit.dondok.domain.crew.entity.QCrewParticipant.crewParticipant;
import static com.oit.dondok.domain.member.entity.QMember.member;
import static com.oit.dondok.domain.mission.entity.QMissionLog.missionLog;

import com.oit.dondok.domain.mission.entity.MissionLog;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MissionLogQueryRepository {

  private final JPAQueryFactory queryFactory;

  public Optional<MissionLog> findByIdWithCrewForModeration(Long missionLogId) {
    return Optional.ofNullable(
        queryFactory
            .selectFrom(missionLog)
            .join(missionLog.crewParticipant, crewParticipant)
            .fetchJoin()
            .join(crewParticipant.crew, crew)
            .fetchJoin()
            .join(crew.hostMember, member)
            .fetchJoin()
            .where(missionLog.id.eq(missionLogId))
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .fetchOne());
  }

  // 특정 크루에서 내가 제출한 인증 로그를 최신순 커서 페이지네이션으로 조회한다.
  public List<MissionLogItemProjection> findMyMissionLogsByCursor(
      UUID memberUuid,
      Long crewId,
      int limit,
      LocalDateTime cursorServerTime,
      Long cursorMissionLogId) {
    return queryFactory
        .select(
            Projections.constructor(
                MissionLogItemProjection.class,
                missionLog.id,
                crewParticipant.id,
                missionLog.imageS3Key,
                missionLog.caption,
                missionLog.imageHash,
                missionLog.serverTime,
                missionLog.exifTakenAt,
                missionLog.certificationStatus,
                missionLog.failureReason,
                missionLog.decisionType,
                missionLog.rejectReasonCode))
        .from(missionLog)
        .join(missionLog.crewParticipant, crewParticipant)
        .join(crewParticipant.crew, crew)
        .join(crewParticipant.member, member)
        .where(
            crew.id.eq(crewId),
            member.uuid.eq(memberUuid),
            buildMissionLogCursorCondition(cursorServerTime, cursorMissionLogId))
        .orderBy(missionLog.serverTime.desc(), missionLog.id.desc())
        .limit(limit)
        .fetch();
  }

  private Predicate buildMissionLogCursorCondition(
      LocalDateTime cursorServerTime, Long cursorMissionLogId) {
    if (cursorServerTime == null || cursorMissionLogId == null) {
      return null;
    }

    return missionLog
        .serverTime
        .lt(cursorServerTime)
        .or(missionLog.serverTime.eq(cursorServerTime).and(missionLog.id.lt(cursorMissionLogId)));
  }
}
