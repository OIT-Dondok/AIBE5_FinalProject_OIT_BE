package com.oit.dondok.domain.mission.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record MissionReviewListResponse(
    List<MissionReviewSummaryItemResponse> items,
    MissionReviewSummaryResponse summary,
    String nextCursor) {}
