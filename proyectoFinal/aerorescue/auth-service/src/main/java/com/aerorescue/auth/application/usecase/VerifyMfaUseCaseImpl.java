package com.aerorescue.auth.application.usecase;

import com.aerorescue.auth.domain.exception.InvalidMfaCodeException;
import com.aerorescue.auth.domain.exception.InvalidTokenException;
import com.aerorescue.auth.domain.model.TokenPair;
import com.aerorescue.auth.domain.model.User;
import com.aerorescue.auth.domain.port.in.VerifyMfaUseCase;
import com.aerorescue.auth.domain.port.out.RefreshTokenRepository;
import com.aerorescue.auth.domain.port.out.TokenService;
import com.aerorescue.auth.domain.port.out.UserRepository;
import com.aerorescue.auth.domain.service.TotpService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerifyMfaUseCaseImpl implements VerifyMfaUseCase {

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final TotpService totpService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiration-hours:8}")
    private int refreshExpirationHours;

    @Override
    public TokenPair verifyMfa(String tempToken, int totpCode) {
        if (!tokenService.isTokenValid(tempToken)) {
            throw new InvalidTokenException("Temp token is invalid or expired");
        }

        UUID userId = tokenService.extractUserIdFromTempToken(tempToken);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new InvalidTokenException("User not found"));

        if (!totpService.verifyCode(user.getMfaSecret(), totpCode)) {
            throw new InvalidMfaCodeException();
        }

        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = tokenService.generateRefreshToken(user.getId());
        Instant expiresAt = Instant.now().plus(refreshExpirationHours, ChronoUnit.HOURS);
        refreshTokenRepository.save(refreshToken, user.getId(), expiresAt);

        return TokenPair.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .accessTokenExpiresIn(15 * 60L)
            .build();
    }
}
