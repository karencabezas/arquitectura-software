package com.authapp.application.auth;

import com.authapp.domain.model.Usuario;
import com.authapp.domain.port.UsuarioRepositoryPort;
import com.authapp.infrastructure.security.JwtService;
import com.authapp.infrastructure.security.TotpService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService — casos adicionales")
class AuthServiceEdgeCasesTest {

    @Mock UsuarioRepositoryPort usuarioPort;
    @Mock JwtService            jwtService;
    @Mock TotpService           totpService;
    @Mock PasswordEncoder       passwordEncoder;

    @InjectMocks AuthService authService;

    // ── validateMfa: usuario no encontrado ────────────────────────────

    @Nested
    @DisplayName("validateMfa() — usuario no encontrado")
    class ValidateMfaUsuarioNoEncontrado {

        @Test
        @DisplayName("lanza AuthException cuando el email del token no corresponde a ningún usuario")
        void usuarioNoEncontrado() {
            when(jwtService.isTokenValid("pre-token")).thenReturn(true);
            when(jwtService.isPreAuthToken("pre-token")).thenReturn(true);
            when(jwtService.extractEmail("pre-token")).thenReturn("fantasma@test.com");
            when(usuarioPort.findByEmail("fantasma@test.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.validateMfa("pre-token", "123456"))
                    .isInstanceOf(AuthService.AuthException.class)
                    .hasMessage("Usuario no encontrado");
        }
    }

    // ── setupMfa: usuario no encontrado ──────────────────────────────

    @Nested
    @DisplayName("setupMfa() — usuario no encontrado")
    class SetupMfaUsuarioNoEncontrado {

        @Test
        @DisplayName("lanza AuthException cuando el email del token no corresponde a ningún usuario")
        void usuarioNoEncontrado() {
            when(jwtService.isTokenValid("pre-token")).thenReturn(true);
            when(jwtService.isPreAuthToken("pre-token")).thenReturn(true);
            when(jwtService.extractEmail("pre-token")).thenReturn("fantasma@test.com");
            when(usuarioPort.findByEmail("fantasma@test.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.setupMfa("pre-token"))
                    .isInstanceOf(AuthService.AuthException.class)
                    .hasMessage("Usuario no encontrado");
        }

        @Test
        @DisplayName("lanza AuthException cuando el preAuthToken es inválido en setupMfa")
        void tokenInvalidoEnSetupMfa() {
            when(jwtService.isTokenValid("bad-token")).thenReturn(false);

            assertThatThrownBy(() -> authService.setupMfa("bad-token"))
                    .isInstanceOf(AuthService.AuthException.class)
                    .hasMessage("Token de pre-autenticación inválido o expirado");
        }
    }

    // ── confirmMfaSetup: usuario no encontrado ────────────────────────

    @Nested
    @DisplayName("confirmMfaSetup() — usuario no encontrado")
    class ConfirmMfaUsuarioNoEncontrado {

        @Test
        @DisplayName("lanza AuthException cuando el email del token no corresponde a ningún usuario")
        void usuarioNoEncontrado() {
            when(jwtService.isTokenValid("pre-token")).thenReturn(true);
            when(jwtService.isPreAuthToken("pre-token")).thenReturn(true);
            when(jwtService.extractEmail("pre-token")).thenReturn("fantasma@test.com");
            when(usuarioPort.findByEmail("fantasma@test.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.confirmMfaSetup("pre-token", "123456"))
                    .isInstanceOf(AuthService.AuthException.class)
                    .hasMessage("Usuario no encontrado");
        }

        @Test
        @DisplayName("lanza AuthException cuando el token no es PRE_AUTH en confirmMfaSetup")
        void tokenNoEsPreAuthEnConfirm() {
            when(jwtService.isTokenValid("full-token")).thenReturn(true);
            when(jwtService.isPreAuthToken("full-token")).thenReturn(false);

            assertThatThrownBy(() -> authService.confirmMfaSetup("full-token", "123456"))
                    .isInstanceOf(AuthService.AuthException.class)
                    .hasMessage("Token de pre-autenticación inválido o expirado");
        }
    }

    // ── login: mensaje de LoginResult ────────────────────────────────

    @Nested
    @DisplayName("LoginResult — mensajes estáticos")
    class LoginResultMensajes {

        @Test
        @DisplayName("mfaRequired() produce mensaje de autenticador")
        void mfaRequiredMensaje() {
            AuthService.LoginResult result = AuthService.LoginResult.mfaRequired("tok");
            assertThat(result.message()).isEqualTo("Ingrese el código de su app autenticadora");
            assertThat(result.mfaRequired()).isTrue();
            assertThat(result.preAuthToken()).isEqualTo("tok");
        }

        @Test
        @DisplayName("mfaNotConfigured() produce mensaje de configuración")
        void mfaNotConfiguredMensaje() {
            AuthService.LoginResult result = AuthService.LoginResult.mfaNotConfigured("tok2");
            assertThat(result.message()).isEqualTo("Configure su autenticador MFA");
            assertThat(result.mfaRequired()).isFalse();
            assertThat(result.preAuthToken()).isEqualTo("tok2");
        }
    }
}
