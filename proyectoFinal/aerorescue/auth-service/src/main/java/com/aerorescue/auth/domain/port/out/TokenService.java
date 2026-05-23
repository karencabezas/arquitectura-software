package com.aerorescue.auth.domain.port.out;

import com.aerorescue.auth.domain.model.User;
import java.util.UUID;

public interface TokenService {
    String generateAccessToken(User user);
    String generateRefreshToken(UUID userId);
    String generateTempToken(UUID userId);
    UUID extractUserIdFromTempToken(String tempToken);
    boolean isTokenValid(String token);
}
