package com.oit.dondok.infra.image.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PresignedUrlRequest(
    @NotNull(message = "crew_id는 필수입니다.") @Positive(message = "crew_id는 양수여야 합니다.") Long crewId,
    @NotNull(message = "crew_participant_id는 필수입니다.")
        @Positive(message = "crew_participant_id는 양수여야 합니다.")
        Long crewParticipantId) {}
