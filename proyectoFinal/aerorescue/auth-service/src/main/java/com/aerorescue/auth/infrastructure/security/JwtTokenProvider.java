package com.aerorescue.auth.infrastructure.security;

import com.aerorescue.auth.domain.model.User;
import com.aerorescue.auth.domain.port.out.TokenService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenProvider implements TokenService {

    @Value("${jwt.private-key-path}")
    private Resource privateKeyResource;

    @Value("${jwt.public-key-path}")
    private Resource publicKeyResource;

    @Value("${jwt.access-token-expiration-minutes:15}")
    private int accessTokenExpirationMinutes;

    @Value("${jwt.temp-token-expiration-minutes:5}")
    private int tempTokenExpirationMinutes;

    @Value("${jwt.issuer:aerorescue-auth-service}")
    private String issuer;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() throws Exception {
        this.privateKey = loadPrivateKey();
        this.publicKey = loadPublicKey();
        log.info("JWT RS256 keys loaded successfully");
    }

    @Override
    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(user.getId().toString())
            .issuer(issuer)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(accessTokenExpirationMinutes, ChronoUnit.MINUTES)))
            .claim("username", user.getUsername())
            .claim("role", user.getRole().name())
            .claim("region", user.getRegion())
            .claim("clearance_level", user.getClearanceLevel())
            .claim("allowed_mission_types", user.getAllowedMissionTypes())
            .claim("organization_id", user.getOrganizationId())
            .claim("token_type", "ACCESS")
            .signWith(privateKey, Jwts.SIG.RS256)
            .compact();
    }

    @Override
    public String generateRefreshToken(UUID userId) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(userId.toString())
            .issuer(issuer)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(8, ChronoUnit.HOURS)))
            .claim("token_type", "REFRESH")
            .signWith(privateKey, Jwts.SIG.RS256)
            .compact();
    }

    @Override
    public String generateTempToken(UUID userId) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(userId.toString())
            .issuer(issuer)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(tempTokenExpirationMinutes, ChronoUnit.MINUTES)))
            .claim("token_type", "TEMP_MFA")
            .signWith(privateKey, Jwts.SIG.RS256)
            .compact();
    }

    @Override
    public UUID extractUserIdFromTempToken(String tempToken) {
        Claims claims = parseClaims(tempToken);
        return UUID.fromString(claims.getSubject());
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(publicKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private PrivateKey loadPrivateKey() throws Exception {
        String keyContent = new String(privateKeyResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(keyContent);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private PublicKey loadPublicKey() throws Exception {
        String keyContent = new String(publicKeyResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(keyContent);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }
}
