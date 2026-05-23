package com.aerorescue.auth.infrastructure.adapter.out.persistence;

import com.aerorescue.auth.domain.port.out.RefreshTokenRepository;
import com.aerorescue.auth.infrastructure.adapter.out.persistence.entity.RefreshTokenEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpaRepository;

    @Override
    public void save(String token, UUID userId, Instant expiresAt) {
        RefreshTokenEntity entity = RefreshTokenEntity.builder()
            .token(token)
            .userId(userId)
            .expiresAt(expiresAt)
            .revoked(false)
            .build();
        jpaRepository.save(entity);
    }

    @Override
    public Optional<UUID> findUserIdByToken(String token) {
        return jpaRepository
            .findByTokenAndRevokedFalseAndExpiresAtAfter(token, Instant.now())
            .map(RefreshTokenEntity::getUserId);
    }

    @Override
    @Transactional
    public void revoke(String token) {
        jpaRepository.revokeByToken(token);
    }

    @Override
    @Transactional
    public void revokeAllByUserId(UUID userId) {
        jpaRepository.revokeAllByUserId(userId);
    }
}
