package com.aerorescue.auth.domain.port.in;

import com.aerorescue.auth.domain.model.TokenPair;

public interface RefreshTokenUseCase {
    TokenPair refresh(String refreshToken);
}
