package com.aerorescue.emergency.infrastructure.adapter.in.rest.dto;

import com.aerorescue.emergency.domain.model.EmergencyStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(@NotNull EmergencyStatus status) {}
