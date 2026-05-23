package com.aerorescue.auth.domain.port.out;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {
    void save(String token, UUID userId, java.time.Instant expiresAt);
    Optional<UUID> findUserIdByToken(String token);
    void revoke(String token);
    void revokeAllByUserId(UUID userId);
}
