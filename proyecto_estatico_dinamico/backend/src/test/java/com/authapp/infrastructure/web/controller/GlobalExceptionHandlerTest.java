package com.authapp.infrastructure.web.controller;

import com.authapp.application.auth.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleAuthException() retorna 401 con error AUTH_ERROR")
    void handleAuthException() {
        AuthService.AuthException ex = new AuthService.AuthException("Credenciales inválidas");

        ResponseEntity<?> response = handler.handleAuthException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body).containsEntry("error", "AUTH_ERROR");
        assertThat(body).containsEntry("message", "Credenciales inválidas");
    }

    @Test
    @DisplayName("handleAccessDenied() retorna 403 con error ACCESS_DENIED")
    void handleAccessDenied() {
        AccessDeniedException ex = new AccessDeniedException("Acceso denegado");

        ResponseEntity<?> response = handler.handleAccessDenied(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body).containsEntry("error", "ACCESS_DENIED");
        assertThat(body).containsEntry("message", "Acceso denegado");
    }

    @Test
    @DisplayName("handleIllegalArgument() retorna 400 con error BAD_REQUEST")
    void handleIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("Argumento inválido");

        ResponseEntity<?> response = handler.handleIllegalArgument(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body).containsEntry("error", "BAD_REQUEST");
        assertThat(body).containsEntry("message", "Argumento inválido");
    }

    @Test
    @DisplayName("handleGeneral() retorna 500 con error SERVER_ERROR")
    void handleGeneral() {
        Exception ex = new RuntimeException("Error inesperado");

        ResponseEntity<?> response = handler.handleGeneral(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body).containsEntry("error", "SERVER_ERROR");
        assertThat(body).containsKey("message");
    }
}
