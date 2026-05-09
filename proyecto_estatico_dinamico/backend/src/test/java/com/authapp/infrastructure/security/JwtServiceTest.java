package com.authapp.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtService")
class JwtServiceTest {

    private JwtService jwtService;

    // Secret Base64 de 256 bits válido para tests
    private static final String SECRET =
            "c29tZXZlcnlsb25nc2VjcmV0a2V5dGhhdGlzYXRsZWFzdDI1NmJpdHNsb25n";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", 86400000L);
        ReflectionTestUtils.setField(jwtService, "preAuthExpiration", 120000L);
    }

    // ═══════════════════════════════════════════════════════════════════
    // PRE-AUTH TOKEN
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("generatePreAuthToken() genera un token válido con type=PRE_AUTH")
    void generarPreAuthToken() {
        String token = jwtService.generatePreAuthToken("karen@test.com");

        assertThat(token).isNotBlank();
        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.isPreAuthToken(token)).isTrue();
        assertThat(jwtService.isFullAuthToken(token)).isFalse();
        assertThat(jwtService.extractEmail(token)).isEqualTo("karen@test.com");
    }

    // ═══════════════════════════════════════════════════════════════════
    // FULL AUTH TOKEN
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("generateFullToken() genera un token válido con type=FULL_AUTH")
    void generarFullToken() {
        String token = jwtService.generateFullToken(
                "karen@test.com", 1L,
                Set.of("ADMIN"),
                Set.of("PRODUCT_SELECT", "PRODUCT_DELETE")
        );

        assertThat(token).isNotBlank();
        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.isFullAuthToken(token)).isTrue();
        assertThat(jwtService.isPreAuthToken(token)).isFalse();
        assertThat(jwtService.extractEmail(token)).isEqualTo("karen@test.com");
    }

    @Test
    @DisplayName("extractClaims() retorna el userId correcto del full token")
    void extractUserId() {
        String token = jwtService.generateFullToken("karen@test.com", 42L, Set.of(), Set.of());

        Long userId = jwtService.extractClaims(token).get("userId", Long.class);

        assertThat(userId).isEqualTo(42L);
    }

    // ═══════════════════════════════════════════════════════════════════
    // VALIDACIÓN
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("isTokenValid() retorna false para un token malformado")
    void tokenMalformado() {
        assertThat(jwtService.isTokenValid("esto.no.es.un.token")).isFalse();
    }

    @Test
    @DisplayName("isTokenValid() retorna false para un token con firma incorrecta")
    void tokenFirmaIncorrecta() {
        String token = jwtService.generatePreAuthToken("karen@test.com");
        String tokenTampeado = token.substring(0, token.length() - 5) + "XXXXX";

        assertThat(jwtService.isTokenValid(tokenTampeado)).isFalse();
    }

    @Test
    @DisplayName("isTokenValid() retorna false para string vacío")
    void tokenVacio() {
        assertThat(jwtService.isTokenValid("")).isFalse();
    }

    @Test
    @DisplayName("isPreAuthToken() retorna false para un full token")
    void fullTokenNoEsPreAuth() {
        String token = jwtService.generateFullToken("karen@test.com", 1L, Set.of(), Set.of());
        assertThat(jwtService.isPreAuthToken(token)).isFalse();
    }

    @Test
    @DisplayName("isFullAuthToken() retorna false para un pre-auth token")
    void preAuthTokenNoEsFullAuth() {
        String token = jwtService.generatePreAuthToken("karen@test.com");
        assertThat(jwtService.isFullAuthToken(token)).isFalse();
    }
}
