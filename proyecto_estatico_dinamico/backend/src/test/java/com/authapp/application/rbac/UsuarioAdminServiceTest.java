package com.authapp.application.rbac;

import com.authapp.domain.model.Usuario;
import com.authapp.domain.port.UsuarioRepositoryPort;
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
@DisplayName("UsuarioAdminService")
class UsuarioAdminServiceTest {

    @Mock UsuarioRepositoryPort usuarioPort;
    @Mock PasswordEncoder       passwordEncoder;

    @InjectMocks UsuarioAdminService usuarioAdminService;

    // ── Helper ──────────────────────────────────────────────────────────

    private Usuario usuarioExistente() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setEmail("karen@test.com");
        u.setPasswordHash("hash_viejo");
        u.setActivo(true);
        u.setMfaEnabled(false);
        return u;
    }

    // ═══════════════════════════════════════════════════════════════════
    // CREAR USUARIO
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("crearUsuario()")
    class Crear {

        @Test
        @DisplayName("crea usuario con password hasheado y mfaEnabled=false")
        void creaCorrectamente() {
            when(usuarioPort.existsByEmail("nuevo@test.com")).thenReturn(false);
            when(passwordEncoder.encode("Password1!")).thenReturn("hashed");
            when(usuarioPort.save(any())).thenAnswer(i -> i.getArgument(0));

            Usuario result = usuarioAdminService.crearUsuario("nuevo@test.com", "Password1!", true);

            assertThat(result.getEmail()).isEqualTo("nuevo@test.com");
            assertThat(result.getPasswordHash()).isEqualTo("hashed");
            assertThat(result.isActivo()).isTrue();
            assertThat(result.isMfaEnabled()).isFalse();
        }

        @Test
        @DisplayName("puede crear usuario con activo=false")
        void creaInactivo() {
            when(usuarioPort.existsByEmail("nuevo@test.com")).thenReturn(false);
            when(passwordEncoder.encode(any())).thenReturn("hashed");
            when(usuarioPort.save(any())).thenAnswer(i -> i.getArgument(0));

            Usuario result = usuarioAdminService.crearUsuario("nuevo@test.com", "Password1!", false);

            assertThat(result.isActivo()).isFalse();
        }

        @Test
        @DisplayName("lanza excepción cuando el email ya existe")
        void emailDuplicado() {
            when(usuarioPort.existsByEmail("karen@test.com")).thenReturn(true);

            assertThatThrownBy(() ->
                usuarioAdminService.crearUsuario("karen@test.com", "Password1!", true)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessage("El email ya está registrado");
        }

        @Test
        @DisplayName("lanza excepción cuando la contraseña es null")
        void passwordNull() {
            when(usuarioPort.existsByEmail("nuevo@test.com")).thenReturn(false);

            assertThatThrownBy(() ->
                usuarioAdminService.crearUsuario("nuevo@test.com", null, true)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessage("La contraseña debe tener al menos 8 caracteres");
        }

        @Test
        @DisplayName("lanza excepción cuando la contraseña tiene menos de 8 caracteres")
        void passwordCorta() {
            when(usuarioPort.existsByEmail("nuevo@test.com")).thenReturn(false);

            assertThatThrownBy(() ->
                usuarioAdminService.crearUsuario("nuevo@test.com", "123", true)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessage("La contraseña debe tener al menos 8 caracteres");
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // ACTUALIZAR USUARIO
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("actualizarUsuario()")
    class Actualizar {

        @Test
        @DisplayName("actualiza email y estado sin cambiar contraseña cuando password viene vacío")
        void actualizaSinCambiarPassword() {
            when(usuarioPort.findById(1L)).thenReturn(Optional.of(usuarioExistente()));
            when(usuarioPort.save(any())).thenAnswer(i -> i.getArgument(0));

            Usuario result = usuarioAdminService.actualizarUsuario(1L, "nuevo@test.com", false, "");

            assertThat(result.getEmail()).isEqualTo("nuevo@test.com");
            assertThat(result.isActivo()).isFalse();
            assertThat(result.getPasswordHash()).isEqualTo("hash_viejo"); // no cambió
            verify(passwordEncoder, never()).encode(any());
        }

        @Test
        @DisplayName("actualiza password cuando se envía uno nuevo válido")
        void actualizaPassword() {
            when(usuarioPort.findById(1L)).thenReturn(Optional.of(usuarioExistente()));
            when(passwordEncoder.encode("NuevoPass1!")).thenReturn("nuevo_hash");
            when(usuarioPort.save(any())).thenAnswer(i -> i.getArgument(0));

            Usuario result = usuarioAdminService.actualizarUsuario(1L, "karen@test.com", true, "NuevoPass1!");

            assertThat(result.getPasswordHash()).isEqualTo("nuevo_hash");
        }

        @Test
        @DisplayName("lanza excepción cuando el usuario no existe")
        void usuarioNoExiste() {
            when(usuarioPort.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                usuarioAdminService.actualizarUsuario(99L, "x@test.com", true, null)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessage("Usuario no encontrado");
        }

        @Test
        @DisplayName("lanza excepción cuando el nuevo email ya está en uso por otro usuario")
        void emailEnUso() {
            when(usuarioPort.findById(1L)).thenReturn(Optional.of(usuarioExistente()));
            when(usuarioPort.existsByEmail("ocupado@test.com")).thenReturn(true);

            assertThatThrownBy(() ->
                usuarioAdminService.actualizarUsuario(1L, "ocupado@test.com", true, null)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessage("El email ya está en uso");
        }

        @Test
        @DisplayName("permite actualizar con el mismo email del usuario")
        void mismoEmailPermitido() {
            when(usuarioPort.findById(1L)).thenReturn(Optional.of(usuarioExistente()));
            when(usuarioPort.save(any())).thenAnswer(i -> i.getArgument(0));

            // mismo email = karen@test.com, no debe verificar duplicado
            assertThatCode(() ->
                usuarioAdminService.actualizarUsuario(1L, "karen@test.com", true, null)
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("lanza excepción cuando el nuevo password tiene menos de 8 caracteres")
        void nuevoPasswordCorto() {
            when(usuarioPort.findById(1L)).thenReturn(Optional.of(usuarioExistente()));

            assertThatThrownBy(() ->
                usuarioAdminService.actualizarUsuario(1L, "karen@test.com", true, "123")
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessage("La contraseña debe tener al menos 8 caracteres");
        }
    }
}
