package com.authapp.application.auth;

import com.authapp.domain.model.Permiso;
import com.authapp.domain.model.Rol;
import com.authapp.domain.model.Usuario;
import com.authapp.domain.port.UsuarioRepositoryPort;
import com.authapp.infrastructure.security.JwtService;
import com.authapp.infrastructure.security.TotpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    @Mock UsuarioRepositoryPort usuarioPort;
    @Mock JwtService            jwtService;
    @Mock TotpService           totpService;
    @Mock PasswordEncoder       passwordEncoder;

    @InjectMocks AuthService authService;

    // ── Helpers ──────────────────────────────────────────────────────────

    private Usuario usuarioActivo(boolean mfaEnabled, String totpSecret) {
        Permiso p = new Permiso(1L, "PRODUCT_SELECT", "Ver productos");
        Rol rol   = new Rol(1L, "USER", "Usuario", Set.of(p));
        Usuario u = new Usuario();
        u.setId(1L);
        u.setEmail("karen@test.com");
        u.setPasswordHash("hashed");
        u.setActivo(true);
        u.setMfaEnabled(mfaEnabled);
        u.setTotpSecret(totpSecret);
        u.setRoles(Set.of(rol));
        return u;
    }

    // ═══════════════════════════════════════════════════════════════════
    // LOGIN
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("retorna mfaRequired=true cuando el usuario tiene MFA configurado")
        void loginConMfaConfigurado() {
            Usuario u = usuarioActivo(true, "SECRET123");
            when(usuarioPort.findByEmail("karen@test.com")).thenReturn(Optional.of(u));
            when(passwordEncoder.matches("pass", "hashed")).thenReturn(true);
            when(jwtService.generatePreAuthToken("karen@test.com")).thenReturn("pre-token");

            AuthService.LoginResult result = authService.login("karen@test.com", "pass");

            assertThat(result.mfaRequired()).isTrue();
            assertThat(result.preAuthToken()).isEqualTo("pre-token");
        }

        @Test
        @DisplayName("retorna mfaRequired=false cuando el usuario NO tiene MFA configurado")
        void loginSinMfa() {
            Usuario u = usuarioActivo(false, null);
            when(usuarioPort.findByEmail("karen@test.com")).thenReturn(Optional.of(u));
            when(passwordEncoder.matches("pass", "hashed")).thenReturn(true);
            when(jwtService.generatePreAuthToken("karen@test.com")).thenReturn("pre-token");

            AuthService.LoginResult result = authService.login("karen@test.com", "pass");

            assertThat(result.mfaRequired()).isFalse();
        }

        @Test
        @DisplayName("lanza AuthException cuando el email no existe")
        void loginEmailNoExiste() {
            when(usuarioPort.findByEmail(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login("noexiste@test.com", "pass"))
                    .isInstanceOf(AuthService.AuthException.class)
                    .hasMessage("Credenciales inválidas");
        }

        @Test
        @DisplayName("lanza AuthException cuando la contraseña es incorrecta")
        void loginPasswordIncorrecta() {
            Usuario u = usuarioActivo(false, null);
            when(usuarioPort.findByEmail("karen@test.com")).thenReturn(Optional.of(u));
            when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

            assertThatThrownBy(() -> authService.login("karen@test.com", "wrong"))
                    .isInstanceOf(AuthService.AuthException.class)
                    .hasMessage("Credenciales inválidas");
        }

        @Test
        @DisplayName("lanza AuthException cuando el usuario está inactivo")
        void loginUsuarioInactivo() {
            Usuario u = usuarioActivo(false, null);
            u.setActivo(false);
            when(usuarioPort.findByEmail("karen@test.com")).thenReturn(Optional.of(u));

            assertThatThrownBy(() -> authService.login("karen@test.com", "pass"))
                    .isInstanceOf(AuthService.AuthException.class)
                    .hasMessage("Usuario inactivo");
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // VALIDATE MFA
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("validateMfa()")
    class ValidateMfa {

        @Test
        @DisplayName("retorna AuthResult con token cuando el código TOTP es correcto")
        void validacionExitosa() {
            Usuario u = usuarioActivo(true, "SECRET123");
            when(jwtService.isTokenValid("pre-token")).thenReturn(true);
            when(jwtService.isPreAuthToken("pre-token")).thenReturn(true);
            when(jwtService.extractEmail("pre-token")).thenReturn("karen@test.com");
            when(usuarioPort.findByEmail("karen@test.com")).thenReturn(Optional.of(u));
            when(totpService.verifyCode("SECRET123", "123456")).thenReturn(true);
            when(jwtService.generateFullToken(any(), any(), any(), any())).thenReturn("full-token");

            AuthService.AuthResult result = authService.validateMfa("pre-token", "123456");

            assertThat(result.token()).isEqualTo("full-token");
            assertThat(result.email()).isEqualTo("karen@test.com");
            assertThat(result.roles()).contains("USER");
        }

        @Test
        @DisplayName("lanza AuthException cuando el código TOTP es incorrecto")
        void codigoTotpIncorrecto() {
            Usuario u = usuarioActivo(true, "SECRET123");
            when(jwtService.isTokenValid("pre-token")).thenReturn(true);
            when(jwtService.isPreAuthToken("pre-token")).thenReturn(true);
            when(jwtService.extractEmail("pre-token")).thenReturn("karen@test.com");
            when(usuarioPort.findByEmail("karen@test.com")).thenReturn(Optional.of(u));
            when(totpService.verifyCode("SECRET123", "000000")).thenReturn(false);

            assertThatThrownBy(() -> authService.validateMfa("pre-token", "000000"))
                    .isInstanceOf(AuthService.AuthException.class)
                    .hasMessage("Código TOTP inválido");
        }

        @Test
        @DisplayName("lanza AuthException cuando el preAuthToken es inválido")
        void preAuthTokenInvalido() {
            when(jwtService.isTokenValid("bad-token")).thenReturn(false);

            assertThatThrownBy(() -> authService.validateMfa("bad-token", "123456"))
                    .isInstanceOf(AuthService.AuthException.class)
                    .hasMessage("Token de pre-autenticación inválido o expirado");
        }

        @Test
        @DisplayName("lanza AuthException cuando el token no es de tipo PRE_AUTH")
        void tokenNoEsPreAuth() {
            when(jwtService.isTokenValid("full-token")).thenReturn(true);
            when(jwtService.isPreAuthToken("full-token")).thenReturn(false);

            assertThatThrownBy(() -> authService.validateMfa("full-token", "123456"))
                    .isInstanceOf(AuthService.AuthException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // SETUP MFA
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("setupMfa()")
    class SetupMfa {

        @Test
        @DisplayName("genera secret y QR y guarda el usuario con mfaEnabled=false")
        void setupExitoso() {
            Usuario u = usuarioActivo(false, null);
            when(jwtService.isTokenValid("pre-token")).thenReturn(true);
            when(jwtService.isPreAuthToken("pre-token")).thenReturn(true);
            when(jwtService.extractEmail("pre-token")).thenReturn("karen@test.com");
            when(usuarioPort.findByEmail("karen@test.com")).thenReturn(Optional.of(u));
            when(totpService.generateSecret()).thenReturn("NEW_SECRET");
            when(totpService.generateQrImageUri("NEW_SECRET", "karen@test.com")).thenReturn("data:image/png;base64,...");
            when(usuarioPort.save(any())).thenReturn(u);

            AuthService.MfaSetupResult result = authService.setupMfa("pre-token");

            assertThat(result.secret()).isEqualTo("NEW_SECRET");
            assertThat(result.qrImageUri()).startsWith("data:image");

            // Verificar que guardó el usuario con el nuevo secret y mfaEnabled=false
            verify(usuarioPort).save(argThat(saved ->
                "NEW_SECRET".equals(saved.getTotpSecret()) && !saved.isMfaEnabled()
            ));
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // CONFIRM MFA SETUP
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("confirmMfaSetup()")
    class ConfirmMfaSetup {

        @Test
        @DisplayName("activa MFA y retorna AuthResult cuando el código es correcto")
        void confirmacionExitosa() {
            Usuario u = usuarioActivo(false, "SECRET123");
            when(jwtService.isTokenValid("pre-token")).thenReturn(true);
            when(jwtService.isPreAuthToken("pre-token")).thenReturn(true);
            when(jwtService.extractEmail("pre-token")).thenReturn("karen@test.com");
            when(usuarioPort.findByEmail("karen@test.com")).thenReturn(Optional.of(u));
            when(totpService.verifyCode("SECRET123", "123456")).thenReturn(true);
            when(usuarioPort.save(any())).thenReturn(u);
            when(jwtService.generateFullToken(any(), any(), any(), any())).thenReturn("full-token");

            AuthService.AuthResult result = authService.confirmMfaSetup("pre-token", "123456");

            assertThat(result.token()).isEqualTo("full-token");
            // Verificar que guardó el usuario con mfaEnabled=true
            verify(usuarioPort).save(argThat(Usuario::isMfaEnabled));
        }

        @Test
        @DisplayName("lanza AuthException cuando el código es incorrecto al confirmar")
        void confirmacionCodigoIncorrecto() {
            Usuario u = usuarioActivo(false, "SECRET123");
            when(jwtService.isTokenValid("pre-token")).thenReturn(true);
            when(jwtService.isPreAuthToken("pre-token")).thenReturn(true);
            when(jwtService.extractEmail("pre-token")).thenReturn("karen@test.com");
            when(usuarioPort.findByEmail("karen@test.com")).thenReturn(Optional.of(u));
            when(totpService.verifyCode("SECRET123", "000000")).thenReturn(false);

            assertThatThrownBy(() -> authService.confirmMfaSetup("pre-token", "000000"))
                    .isInstanceOf(AuthService.AuthException.class)
                    .hasMessageContaining("Código inválido");
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // REGISTER
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("crea el usuario con password hasheado y mfaEnabled=false")
        void registroExitoso() {
            when(usuarioPort.existsByEmail("nuevo@test.com")).thenReturn(false);
            when(passwordEncoder.encode("pass123")).thenReturn("hashed123");
            when(usuarioPort.save(any())).thenAnswer(i -> i.getArgument(0));

            authService.register("nuevo@test.com", "pass123");

            verify(usuarioPort).save(argThat(u ->
                "nuevo@test.com".equals(u.getEmail()) &&
                "hashed123".equals(u.getPasswordHash()) &&
                !u.isMfaEnabled() &&
                u.isActivo()
            ));
        }

        @Test
        @DisplayName("lanza AuthException cuando el email ya está registrado")
        void emailDuplicado() {
            when(usuarioPort.existsByEmail("karen@test.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.register("karen@test.com", "pass123"))
                    .isInstanceOf(AuthService.AuthException.class)
                    .hasMessage("El email ya está registrado");
        }
    }
}
