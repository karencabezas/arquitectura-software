package com.aerorescue.auth.domain.port.in;

public interface LoginUseCase {
    LoginResult login(String username, String password);

    record LoginResult(
        boolean mfaRequired,
        String tempToken,
        com.aerorescue.auth.domain.model.TokenPair tokenPair
    ) {}
}
