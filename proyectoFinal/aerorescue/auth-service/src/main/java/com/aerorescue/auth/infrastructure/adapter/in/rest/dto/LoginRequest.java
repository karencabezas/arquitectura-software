package com.aerorescue.auth.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank String username,
    @NotBlank String password
) {}
