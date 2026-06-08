package com.oit.dondok.domain.mission.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MissionModerationRejectRequest(
    @JsonProperty("reject_reason_code") String rejectReasonCode,
    @JsonProperty("reject_memo") String rejectMemo) {}
