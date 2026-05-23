package com.aerorescue.auth.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(@NotBlank String refreshToken) {}
