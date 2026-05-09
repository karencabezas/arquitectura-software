package com.authapp.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthFilter")
class JwtAuthFilterTest {

    @Mock JwtService jwtService;
    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    @Mock FilterChain filterChain;

    @InjectMocks JwtAuthFilter filter;

    @BeforeEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("pasa la cadena sin autenticar cuando no hay header Authorization")
    void sinHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("pasa la cadena sin autenticar cuando el header no empieza con Bearer")
    void headerSinBearer() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("pasa la cadena sin autenticar cuando el token no es válido")
    void tokenInvalido() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer bad.token");
        when(jwtService.isTokenValid("bad.token")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("pasa la cadena sin autenticar cuando el token es PRE_AUTH (no FULL_AUTH)")
    void tokenPreAuth() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer pre.token");
        when(jwtService.isTokenValid("pre.token")).thenReturn(true);
        when(jwtService.isFullAuthToken("pre.token")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("establece autenticación en el SecurityContext con token FULL_AUTH válido")
    void tokenFullAuthValido() throws Exception {
        String token = "valid.full.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.isFullAuthToken(token)).thenReturn(true);
        when(jwtService.extractEmail(token)).thenReturn("karen@test.com");

        io.jsonwebtoken.Claims claims = mock(io.jsonwebtoken.Claims.class);
        when(jwtService.extractClaims(token)).thenReturn(claims);
        when(claims.get("roles")).thenReturn(List.of("ADMIN"));
        when(claims.get("permissions")).thenReturn(List.of("PRODUCT_SELECT"));
        when(claims.get("userId", Long.class)).thenReturn(1L);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.isAuthenticated()).isTrue();
        AppUserPrincipal principal = (AppUserPrincipal) auth.getPrincipal();
        assertThat(principal.getEmail()).isEqualTo("karen@test.com");
        assertThat(principal.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("sigue la cadena aunque ocurra una excepción al procesar el token")
    void excepcionAlProcesar() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer bad.token");
        when(jwtService.isTokenValid("bad.token")).thenThrow(new RuntimeException("parse error"));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}
