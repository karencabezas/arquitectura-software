package com.aerorescue.auth.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VerifyMfaRequest(
    @NotBlank String tempToken,
    @NotNull Integer totpCode
) {}
