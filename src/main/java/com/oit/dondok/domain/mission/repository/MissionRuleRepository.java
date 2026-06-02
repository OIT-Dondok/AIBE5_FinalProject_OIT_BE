package com.oit.dondok.domain.mission.repository;

import com.oit.dondok.domain.mission.entity.MissionRule;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRuleRepository extends JpaRepository<MissionRule, Long> {
  // 크루의 미션 규칙 조회
  Optional<MissionRule> findByCrewId(Long crewId);
}
