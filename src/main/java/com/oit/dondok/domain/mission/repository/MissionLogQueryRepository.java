package com.oit.dondok.domain.mission.repository;

import static com.oit.dondok.domain.crew.entity.QCrew.crew;
import static com.oit.dondok.domain.crew.entity.QCrewParticipant.crewParticipant;
import static com.oit.dondok.domain.member.entity.QMember.member;
import static com.oit.dondok.domain.mission.entity.QMissionLog.missionLog;
import static com.oit.dondok.domain.mission.entity.QMissionRule.missionRule;
import static com.oit.dondok.domain.settlement.entity.QSettlement.settlement;

import com.oit.dondok.domain.crew.entity.CrewParticipantStatus;
import com.oit.dondok.domain.crew.entity.CrewStatus;
import com.oit.dondok.domain.mission.entity.CertificationStatus;
import com.oit.dondok.domain.mission.entity.DailySettlementType;
import com.oit.dondok.domain.mission.entity.MissionLog;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;
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

  public List<Long> findAutoCertificationCandidateIds(LocalDateTime now, int limit) {
    return queryFactory
        .select(missionLog.id)
        .from(missionLog)
        .join(missionLog.crewParticipant, crewParticipant)
        .join(crewParticipant.crew, crew)
        .join(missionRule)
        .on(missionRule.crew.id.eq(crew.id))
        .where(
            missionLog.certificationStatus.eq(CertificationStatus.PENDING_REVIEW),
            crew.status.eq(CrewStatus.ACTIVE),
            crewParticipant.status.eq(CrewParticipantStatus.LOCKED),
            autoCertificationDue(now),
            JPAExpressions.selectOne()
                .from(settlement)
                .where(settlement.crew.id.eq(crew.id))
                .notExists())
        .orderBy(missionLog.serverTime.asc(), missionLog.id.asc())
        .limit(limit)
        .fetch();
  }

  // 자동 인증 직전 최신 상태를 확인하기 위해 대상 로그를 쓰기 잠금으로 조회한다.
  public Optional<MissionLog> findByIdWithCrewForAutoCertification(Long missionLogId) {
    return Optional.ofNullable(
        queryFactory
            .selectFrom(missionLog)
            .join(missionLog.crewParticipant, crewParticipant)
            .fetchJoin()
            .join(crewParticipant.crew, crew)
            .fetchJoin()
            .where(missionLog.id.eq(missionLogId))
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .fetchOne());
  }

  // 타입별 자동 인증 시각이 지난 로그만 후보로 남기기 위한 DB 필터다.
  private BooleanExpression autoCertificationDue(LocalDateTime now) {
    LocalDate today = now.toLocalDate();

    LocalDateTime typeACutoffExclusive =
        (now.toLocalTime().isBefore(LocalTime.NOON) ? today : today.plusDays(1)).atStartOfDay();
    LocalDateTime typeBCutoffExclusive = today.atStartOfDay();
    LocalDateTime typeCCutoffExclusive =
        (now.toLocalTime().isBefore(LocalTime.NOON) ? today.minusDays(1) : today).atStartOfDay();

    return missionRule
        .dailySettlementType
        .eq(DailySettlementType.A)
        .and(missionLog.serverTime.lt(typeACutoffExclusive))
        .or(
            missionRule
                .dailySettlementType
                .eq(DailySettlementType.B)
                .and(missionLog.serverTime.lt(typeBCutoffExclusive)))
        .or(
            missionRule
                .dailySettlementType
                .eq(DailySettlementType.C)
                .and(missionLog.serverTime.lt(typeCCutoffExclusive)));
  }
}
