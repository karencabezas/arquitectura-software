package com.aerorescue.auth.domain.port.in;

public interface EnrollMfaUseCase {
    EnrollResult enroll(String userId);

    record EnrollResult(String secret, String qrCodeUrl) {}
}
