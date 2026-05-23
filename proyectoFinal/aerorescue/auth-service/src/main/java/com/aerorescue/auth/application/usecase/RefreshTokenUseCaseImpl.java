package com.aerorescue.auth.application.usecase;

import com.aerorescue.auth.domain.exception.InvalidTokenException;
import com.aerorescue.auth.domain.model.TokenPair;
import com.aerorescue.auth.domain.model.User;
import com.aerorescue.auth.domain.port.in.RefreshTokenUseCase;
import com.aerorescue.auth.domain.port.out.RefreshTokenRepository;
import com.aerorescue.auth.domain.port.out.TokenService;
import com.aerorescue.auth.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenUseCaseImpl implements RefreshTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    @Value("${jwt.refresh-token-expiration-hours:8}")
    private int refreshExpirationHours;

    @Override
    public TokenPair refresh(String refreshToken) {
        UUID userId = refreshTokenRepository.findUserIdByToken(refreshToken)
            .orElseThrow(() -> new InvalidTokenException("Refresh token invalid or expired"));

        refreshTokenRepository.revoke(refreshToken);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new InvalidTokenException("User not found"));

        String newAccessToken = tokenService.generateAccessToken(user);
        String newRefreshToken = tokenService.generateRefreshToken(user.getId());
        Instant expiresAt = Instant.now().plus(refreshExpirationHours, ChronoUnit.HOURS);
        refreshTokenRepository.save(newRefreshToken, user.getId(), expiresAt);

        return TokenPair.builder()
            .accessToken(newAccessToken)
            .refreshToken(newRefreshToken)
            .accessTokenExpiresIn(15 * 60L)
            .build();
    }
}
