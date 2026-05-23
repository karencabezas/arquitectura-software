package com.aerorescue.auth.infrastructure.adapter.in.rest.dto;

public record TokenResponse(String accessToken, String refreshToken, long expiresIn) {}
