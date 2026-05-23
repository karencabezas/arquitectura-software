package com.aerorescue.auth.infrastructure.adapter.in.rest.dto;

public record LoginResponse(
    String status,
    String tempToken,
    String accessToken,
    String refreshToken,
    Long expiresIn
) {
    public static LoginResponse mfaRequired(String tempToken) {
        return new LoginResponse("MFA_REQUIRED", tempToken, null, null, null);
    }

    public static LoginResponse success(String accessToken, String refreshToken, long expiresIn) {
        return new LoginResponse("SUCCESS", null, accessToken, refreshToken, expiresIn);
    }
}
