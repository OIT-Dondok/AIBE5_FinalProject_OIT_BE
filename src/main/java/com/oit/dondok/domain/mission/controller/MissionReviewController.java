package com.oit.dondok.domain.mission.controller;

import com.oit.dondok.domain.mission.dto.response.MissionReviewListResponse;
import com.oit.dondok.domain.mission.service.MissionReviewQueryService;
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

@Tag(name = "미션 검토 목록", description = "방장 미션 인증 검토 목록 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crews/{crewId}/mission-logs/moderation")
public class MissionReviewController {

  private final MissionReviewQueryService missionReviewQueryService;

  @Operation(summary = "방장 미션 인증 검토 목록 조회", description = "방장이 특정 크루의 인증을 긴급/주의/일반 검토 기준으로 조회합니다.")
  @GetMapping
  public ResponseEntity<MissionReviewListResponse> getReviewList(
      @AuthenticationPrincipal UUID memberUuid,
      @PathVariable Long crewId,
      @RequestParam(name = "review_category", required = false) String reviewCategory,
      @RequestParam(required = false) String cursor,
      @RequestParam(defaultValue = "20") int limit) {
    return ResponseEntity.ok(
        missionReviewQueryService.getReviewList(memberUuid, crewId, reviewCategory, cursor, limit));
  }
}
