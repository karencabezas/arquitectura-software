package com.aerorescue.auth.domain.port.in;

import com.aerorescue.auth.domain.model.TokenPair;

public interface VerifyMfaUseCase {
    TokenPair verifyMfa(String tempToken, int totpCode);
}
