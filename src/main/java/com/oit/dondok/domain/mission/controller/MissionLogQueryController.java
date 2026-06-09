package com.oit.dondok.domain.mission.controller;

import com.oit.dondok.domain.mission.dto.response.MissionLogListResponse;
import com.oit.dondok.domain.mission.service.MissionLogQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "미션 인증 조회", description = "미션 인증 로그 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crews/{crewId}/mission-logs")
public class MissionLogQueryController {

  private final MissionLogQueryService missionLogQueryService;

  // 특정 크루에서 현재 사용자가 제출한 미션 인증 로그 목록을 조회한다.
  @Operation(
      summary = "내 크루 미션 인증 로그 조회",
      description = "특정 크루에서 내가 제출한 미션 인증 로그를 커서 페이지네이션으로 조회합니다.")
  @GetMapping("/me")
  public ResponseEntity<MissionLogListResponse> getMyMissionLogs(
      @AuthenticationPrincipal UUID memberUuid,
      @PathVariable Long crewId,
      @RequestParam(required = false) Integer limit,
      @RequestParam(required = false) String cursor) {
    return ResponseEntity.ok(
        missionLogQueryService.findMyMissionLogs(memberUuid, crewId, limit, cursor));
  }
}
