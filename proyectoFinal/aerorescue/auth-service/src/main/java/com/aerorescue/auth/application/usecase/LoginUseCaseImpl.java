package com.aerorescue.auth.application.usecase;

import com.aerorescue.auth.domain.exception.InvalidCredentialsException;
import com.aerorescue.auth.domain.model.TokenPair;
import com.aerorescue.auth.domain.model.User;
import com.aerorescue.auth.domain.port.in.LoginUseCase;
import com.aerorescue.auth.domain.port.out.RefreshTokenRepository;
import com.aerorescue.auth.domain.port.out.TokenService;
import com.aerorescue.auth.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class LoginUseCaseImpl implements LoginUseCase {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.refresh-token-expiration-hours:8}")
    private int refreshExpirationHours;

    @Override
    public LoginResult login(String username, String password) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(InvalidCredentialsException::new);

        if (!user.isActive()) throw new InvalidCredentialsException();
        System.out.println("RAW PASSWORD: " + password);
        System.out.println("DB HASH: " + user.getPassword());
        System.out.println("MATCH RESULT: " + passwordEncoder.matches(password, user.getPassword()));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        // Si MFA está habilitado → devolver tempToken
        if (user.isMfaEnabled()) {
            String tempToken = tokenService.generateTempToken(user.getId());
            return new LoginResult(true, tempToken, null);
        }

        // Sin MFA → devolver tokens directamente
        return new LoginResult(false, null, buildTokenPair(user));
    }

    private TokenPair buildTokenPair(User user) {
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
