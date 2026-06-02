package com.oit.dondok.domain.mission.repository;

import com.oit.dondok.domain.crew.entity.Crew;
import com.oit.dondok.domain.mission.entity.MissionRule;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRuleRepository extends JpaRepository<MissionRule, Long> {
  // 크루의 미션 규칙 조회
  Optional<MissionRule> findByCrew_Id(Long crewId);

  Long crew(Crew crew);
}
