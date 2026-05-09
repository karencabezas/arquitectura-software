package com.authapp.infrastructure.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;

@Service
@Slf4j
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration}")
    private long expiration;

    @Value("${app.jwt.pre-auth-expiration}")
    private long preAuthExpiration;

    private Key getKey() {
        return Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
    }

    // Token temporal (Fase 1 login - antes del MFA)
    public String generatePreAuthToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .claim("type", "PRE_AUTH")
                .claim("mfa_pending", true)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + preAuthExpiration))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Token final (Fase 2 - después del MFA)
    public String generateFullToken(String email, Long userId, Set<String> roles, Set<String> permissions) {
        return Jwts.builder()
                .setSubject(email)
                .claim("type", "FULL_AUTH")
                .claim("userId", userId)
                .claim("roles", roles)
                .claim("permissions", permissions)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractTokenType(String token) {
        return (String) extractClaims(token).get("type");
    }

    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    public boolean isPreAuthToken(String token) {
        return "PRE_AUTH".equals(extractTokenType(token));
    }

    public boolean isFullAuthToken(String token) {
        return "FULL_AUTH".equals(extractTokenType(token));
    }
}
