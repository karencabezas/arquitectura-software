package com.authapp.infrastructure.web.controller;

import com.authapp.application.auth.AuthService;
import com.authapp.infrastructure.security.AppUserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({com.authapp.infrastructure.security.SecurityConfig.class,
         com.authapp.infrastructure.security.JwtAuthFilter.class,
         com.authapp.infrastructure.security.JwtService.class})
@DisplayName("AuthController")
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean AuthService authService;

    // ── /register ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/auth/register")
    class Register {

        @Test
        @DisplayName("retorna 200 cuando el registro es exitoso")
        void registroExitoso() throws Exception {
            doNothing().when(authService).register(anyString(), anyString());

            mockMvc.perform(post("/api/auth/register")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"karen@test.com\",\"password\":\"secret123\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Usuario registrado exitosamente"));
        }

        @Test
        @DisplayName("retorna 401 cuando el servicio lanza AuthException")
        void registroDuplicado() throws Exception {
            doThrow(new AuthService.AuthException("Email ya existe"))
                    .when(authService).register(anyString(), anyString());

            mockMvc.perform(post("/api/auth/register")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"karen@test.com\",\"password\":\"secret123\"}"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("AUTH_ERROR"));
        }
    }

    // ── /login ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/auth/login")
    class Login {

        @Test
        @DisplayName("retorna preAuthToken y mfaRequired en login exitoso")
        void loginExitoso() throws Exception {
            AuthService.LoginResult result = new AuthService.LoginResult("pre-token", true, "OK");
            when(authService.login(anyString(), anyString())).thenReturn(result);

            mockMvc.perform(post("/api/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"karen@test.com\",\"password\":\"secret123\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.preAuthToken").value("pre-token"))
                    .andExpect(jsonPath("$.mfaRequired").value(true));
        }

        @Test
        @DisplayName("retorna 401 con credenciales incorrectas")
        void loginFallido() throws Exception {
            when(authService.login(anyString(), anyString()))
                    .thenThrow(new AuthService.AuthException("Credenciales inválidas"));

            mockMvc.perform(post("/api/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"karen@test.com\",\"password\":\"wrong\"}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ── /mfa/validate ──────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/auth/mfa/validate")
    class ValidateMfa {

        @Test
        @DisplayName("retorna token completo tras validar código MFA correcto")
        void mfaValido() throws Exception {
            AuthService.AuthResult result = new AuthService.AuthResult(
                    "full-token", "karen@test.com", Set.of("USER"), Set.of("PRODUCT_SELECT"));
            when(authService.validateMfa(anyString(), anyString())).thenReturn(result);

            mockMvc.perform(post("/api/auth/mfa/validate")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"preAuthToken\":\"pre-token\",\"totpCode\":\"123456\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("full-token"))
                    .andExpect(jsonPath("$.email").value("karen@test.com"));
        }
    }

    // ── /mfa/setup ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/auth/mfa/setup")
    class SetupMfa {

        @Test
        @DisplayName("retorna qrImageUri y secret en setup de MFA")
        void setupMfa() throws Exception {
            AuthService.MfaSetupResult result = new AuthService.MfaSetupResult("data:image/png", "SECRET123");
            when(authService.setupMfa(anyString())).thenReturn(result);

            mockMvc.perform(post("/api/auth/mfa/setup")
                    .with(csrf())
                    .header("Authorization", "Bearer pre-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.secret").value("SECRET123"))
                    .andExpect(jsonPath("$.qrImageUri").value("data:image/png"));
        }
    }

    // ── /mfa/setup/confirm ─────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/auth/mfa/setup/confirm")
    class ConfirmMfaSetup {

        @Test
        @DisplayName("retorna token completo tras confirmar setup de MFA")
        void confirmMfaSetup() throws Exception {
            AuthService.AuthResult result = new AuthService.AuthResult(
                    "full-token", "karen@test.com", Set.of("USER"), Set.of());
            when(authService.confirmMfaSetup(anyString(), anyString())).thenReturn(result);

            mockMvc.perform(post("/api/auth/mfa/setup/confirm")
                    .with(csrf())
                    .header("Authorization", "Bearer pre-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"totpCode\":\"123456\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("full-token"))
                    .andExpect(jsonPath("$.message").value("MFA configurado exitosamente"));
        }
    }

    // ── /me ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/auth/me")
    class Me {

        @Test
        @DisplayName("retorna datos del usuario autenticado")
        void meAutenticado() throws Exception {
            AppUserPrincipal principal = new AppUserPrincipal(1L, "karen@test.com",
                    List.of("ADMIN"), List.of("PRODUCT_SELECT"));

            // Inyectamos AppUserPrincipal directamente como principal del Authentication
            org.springframework.security.core.Authentication auth =
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            principal, null,
                            List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN")));

            mockMvc.perform(get("/api/auth/me")
                    .with(SecurityMockMvcRequestPostProcessors.authentication(auth)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.email").value("karen@test.com"))
                    .andExpect(jsonPath("$.roles[0]").value("ADMIN"))
                    .andExpect(jsonPath("$.permissions[0]").value("PRODUCT_SELECT"));
        }
    }
}
