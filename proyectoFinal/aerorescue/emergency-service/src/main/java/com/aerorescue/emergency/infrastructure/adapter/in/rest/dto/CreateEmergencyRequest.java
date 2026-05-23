package com.aerorescue.emergency.infrastructure.adapter.in.rest.dto;

import com.aerorescue.emergency.domain.model.EmergencyPriority;
import com.aerorescue.emergency.domain.model.EmergencyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateEmergencyRequest(
    @NotNull EmergencyPriority priority,
    @NotNull EmergencyType type,
    @NotBlank String address,
    @NotBlank String region,
    @NotNull Double lat,
    @NotNull Double lng,
    String description
) {}
