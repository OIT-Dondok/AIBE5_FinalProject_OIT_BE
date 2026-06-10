package com.oit.dondok.domain.mission.repository;

import static com.oit.dondok.domain.crew.entity.QCrew.crew;
import static com.oit.dondok.domain.crew.entity.QCrewParticipant.crewParticipant;
import static com.oit.dondok.domain.member.entity.QMember.member;
import static com.oit.dondok.domain.mission.entity.QMissionLog.missionLog;
import static com.oit.dondok.domain.mission.entity.QMissionRule.missionRule;
import static com.oit.dondok.domain.settlement.entity.QSettlement.settlement;

import com.oit.dondok.domain.crew.entity.CrewParticipantStatus;
import com.oit.dondok.domain.crew.entity.CrewStatus;
import com.oit.dondok.domain.mission.dto.response.MissionReviewSummaryItemResponse.MissionReviewCategory;
import com.oit.dondok.domain.mission.entity.CertificationStatus;
import com.oit.dondok.domain.mission.entity.DailySettlementType;
import com.oit.dondok.domain.mission.entity.ExifRisk;
import com.oit.dondok.domain.mission.entity.MissionLog;
import com.oit.dondok.domain.mission.entity.ModerationDecisionType;
import com.oit.dondok.domain.mission.entity.RejectReasonCode;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MissionLogQueryRepository {

  private final JPAQueryFactory queryFactory;

  public record MissionReviewCandidateRow(
      MissionLog missionLog,
      Long crewId,
      Long crewParticipantId,
      UUID submitterMemberUuid,
      String submitterNickname) {}

  public record MissionReviewItemProjection(
      Long missionLogId,
      Long crewId,
      Long crewParticipantId,
      UUID submitterMemberUuid,
      String submitterNickname,
      String imageS3Key,
      String caption,
      LocalDateTime serverTime,
      LocalDateTime exifTakenAt,
      ExifRisk exifRisk,
      boolean duplicateHash,
      CertificationStatus certificationStatus,
      ModerationDecisionType decisionType,
      RejectReasonCode rejectReasonCode) {}

  public record MissionReviewSummaryCount(long urgentCount, long cautionCount, long normalCount) {}

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

  // 방장 검토 목록에서 분류할 인증 후보를 조회한다.
  public List<MissionReviewCandidateRow> findReviewCandidatesByCrewId(
      Long crewId, LocalDateTime reviewWindowStart) {
    List<Tuple> rows =
        queryFactory
            .select(missionLog, crew.id, crewParticipant.id, member.uuid, member.nickname)
            .from(missionLog)
            .join(missionLog.crewParticipant, crewParticipant)
            .join(crewParticipant.crew, crew)
            .join(crewParticipant.member, member)
            .where(
                crew.id.eq(crewId),
                crew.status.eq(CrewStatus.ACTIVE),
                crewParticipant.status.eq(CrewParticipantStatus.LOCKED),
                missionLog.serverTime.goe(reviewWindowStart),
                reviewCandidate())
            .fetch();

    return rows.stream()
        .map(
            row ->
                new MissionReviewCandidateRow(
                    row.get(missionLog),
                    row.get(crew.id),
                    row.get(crewParticipant.id),
                    row.get(member.uuid),
                    row.get(member.nickname)))
        .toList();
  }

  public List<MissionReviewItemProjection> findReviewItemsByCursor(
      Long crewId,
      LocalDateTime reviewWindowStart,
      DailySettlementType dailySettlementType,
      MissionReviewCategory reviewCategory,
      LocalDateTime now,
      int limit,
      LocalDateTime cursorServerTime,
      Long cursorMissionLogId) {
    return queryFactory
        .select(
            Projections.constructor(
                MissionReviewItemProjection.class,
                missionLog.id,
                crew.id,
                crewParticipant.id,
                member.uuid,
                member.nickname,
                missionLog.imageS3Key,
                missionLog.caption,
                missionLog.serverTime,
                missionLog.exifTakenAt,
                missionLog.exifRisk,
                missionLog.duplicateHash,
                missionLog.certificationStatus,
                missionLog.decisionType,
                missionLog.rejectReasonCode))
        .from(missionLog)
        .join(missionLog.crewParticipant, crewParticipant)
        .join(crewParticipant.crew, crew)
        .join(crewParticipant.member, member)
        .where(
            reviewBaseCondition(crewId, reviewWindowStart),
            reviewCategoryCondition(dailySettlementType, reviewCategory, now),
            reviewCursorCondition(cursorServerTime, cursorMissionLogId))
        .orderBy(missionLog.serverTime.asc(), missionLog.id.asc())
        .limit(limit)
        .fetch();
  }

  public MissionReviewSummaryCount countReviewCandidatesByCategory(
      Long crewId,
      LocalDateTime reviewWindowStart,
      DailySettlementType dailySettlementType,
      LocalDateTime now) {
    return new MissionReviewSummaryCount(
        countReviewCandidatesByCategory(
            crewId,
            reviewWindowStart,
            dailySettlementType,
            MissionReviewCategory.URGENT_REVIEW,
            now),
        countReviewCandidatesByCategory(
            crewId,
            reviewWindowStart,
            dailySettlementType,
            MissionReviewCategory.CAUTION_REVIEW,
            now),
        countReviewCandidatesByCategory(
            crewId,
            reviewWindowStart,
            dailySettlementType,
            MissionReviewCategory.NORMAL_REVIEW,
            now));
  }

  private long countReviewCandidatesByCategory(
      Long crewId,
      LocalDateTime reviewWindowStart,
      DailySettlementType dailySettlementType,
      MissionReviewCategory reviewCategory,
      LocalDateTime now) {
    Long count =
        queryFactory
            .select(missionLog.id.count())
            .from(missionLog)
            .join(missionLog.crewParticipant, crewParticipant)
            .join(crewParticipant.crew, crew)
            .where(
                reviewBaseCondition(crewId, reviewWindowStart),
                reviewCategoryCondition(dailySettlementType, reviewCategory, now))
            .fetchOne();
    return count == null ? 0 : count;
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

  // 수동 처리 전이거나 자동 처리된 인증만 방장 검토 목록 후보로 남긴다.
  private BooleanExpression reviewCandidate() {
    return missionLog
        .certificationStatus
        .eq(CertificationStatus.PENDING_REVIEW)
        .or(
            missionLog.decisionType.in(
                ModerationDecisionType.AUTO_APPROVE, ModerationDecisionType.AUTO_REJECT));
  }

  private BooleanExpression reviewBaseCondition(Long crewId, LocalDateTime reviewWindowStart) {
    return crew.id
        .eq(crewId)
        .and(crew.status.eq(CrewStatus.ACTIVE))
        .and(crewParticipant.status.eq(CrewParticipantStatus.LOCKED))
        .and(missionLog.serverTime.goe(reviewWindowStart))
        .and(reviewCandidate());
  }

  private Predicate reviewCategoryCondition(
      DailySettlementType dailySettlementType,
      MissionReviewCategory reviewCategory,
      LocalDateTime now) {
    BooleanExpression dateCondition =
        missionDateCondition(dailySettlementType, reviewCategory, now);
    if (dateCondition == null) {
      return missionLog.id.isNull();
    }

    return switch (reviewCategory) {
      case URGENT_REVIEW ->
          dateCondition.and(
              missionLog
                  .certificationStatus
                  .eq(CertificationStatus.PENDING_REVIEW)
                  .or(
                      missionLog.decisionType.in(
                          ModerationDecisionType.AUTO_APPROVE,
                          ModerationDecisionType.AUTO_REJECT)));
      case CAUTION_REVIEW ->
          dateCondition
              .and(missionLog.certificationStatus.eq(CertificationStatus.PENDING_REVIEW))
              .and(missionLog.exifRisk.ne(ExifRisk.NORMAL).or(missionLog.exifTakenAt.isNull()));
      case NORMAL_REVIEW ->
          dateCondition
              .and(missionLog.certificationStatus.eq(CertificationStatus.PENDING_REVIEW))
              .and(missionLog.exifRisk.eq(ExifRisk.NORMAL))
              .and(missionLog.exifTakenAt.isNotNull());
    };
  }

  private BooleanExpression missionDateCondition(
      DailySettlementType dailySettlementType,
      MissionReviewCategory reviewCategory,
      LocalDateTime now) {
    LocalDate startDate = now.toLocalDate().minusDays(5);
    LocalDate endDate = now.toLocalDate();
    List<BooleanExpression> dateConditions = new ArrayList<>();

    for (LocalDate missionDate = startDate;
        !missionDate.isAfter(endDate);
        missionDate = missionDate.plusDays(1)) {
      LocalDateTime reviewDeadlineAt = dailySettlementType.autoCertificationAt(missionDate);
      boolean matches =
          switch (reviewCategory) {
            case URGENT_REVIEW ->
                !now.isBefore(reviewDeadlineAt) && !now.isAfter(reviewDeadlineAt.plusHours(72));
            case CAUTION_REVIEW, NORMAL_REVIEW -> now.isBefore(reviewDeadlineAt);
          };
      if (matches) {
        LocalDateTime start = missionDate.atStartOfDay();
        dateConditions.add(
            missionLog.serverTime.goe(start).and(missionLog.serverTime.lt(start.plusDays(1))));
      }
    }

    if (dateConditions.isEmpty()) {
      return null;
    }

    BooleanExpression combined = dateConditions.get(0);
    for (int i = 1; i < dateConditions.size(); i++) {
      combined = combined.or(dateConditions.get(i));
    }
    return combined;
  }

  private Predicate reviewCursorCondition(LocalDateTime cursorServerTime, Long cursorMissionLogId) {
    if (cursorServerTime == null || cursorMissionLogId == null) {
      return null;
    }

    return missionLog
        .serverTime
        .gt(cursorServerTime)
        .or(missionLog.serverTime.eq(cursorServerTime).and(missionLog.id.gt(cursorMissionLogId)));
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
