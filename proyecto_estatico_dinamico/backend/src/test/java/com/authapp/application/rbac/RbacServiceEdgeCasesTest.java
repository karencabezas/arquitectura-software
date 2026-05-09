package com.authapp.application.rbac;

import com.authapp.domain.model.Permiso;
import com.authapp.domain.model.Rol;
import com.authapp.domain.model.Usuario;
import com.authapp.domain.port.PermisoRepositoryPort;
import com.authapp.domain.port.RolRepositoryPort;
import com.authapp.domain.port.UsuarioRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RbacService — casos adicionales")
class RbacServiceEdgeCasesTest {

    @Mock RolRepositoryPort     rolPort;
    @Mock PermisoRepositoryPort permisoPort;
    @Mock UsuarioRepositoryPort usuarioPort;

    @InjectMocks RbacService rbacService;

    private Rol rolConPermisos() {
        Permiso p1 = new Permiso(1L, "PRODUCT_SELECT", "Ver");
        Permiso p2 = new Permiso(2L, "PRODUCT_DELETE", "Eliminar");
        return new Rol(1L, "ADMIN", "Admin", new HashSet<>(Set.of(p1, p2)));
    }

    // ── removerPermisoDeRol ────────────────────────────────────────────

    @Nested
    @DisplayName("removerPermisoDeRol()")
    class RemoverPermiso {

        @Test
        @DisplayName("lanza excepción cuando el rol no existe")
        void rolNoExiste() {
            when(rolPort.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> rbacService.removerPermisoDeRol(99L, 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Rol no encontrado");
        }

        @Test
        @DisplayName("no falla si el permiso a remover no estaba en el rol")
        void permisoNoExistiaEnRol() {
            Rol rol = new Rol(1L, "USER", "Usuario", new HashSet<>());
            when(rolPort.findById(1L)).thenReturn(Optional.of(rol));
            when(rolPort.save(any())).thenAnswer(i -> i.getArgument(0));

            // no debe lanzar excepción
            assertThatCode(() -> rbacService.removerPermisoDeRol(1L, 99L))
                    .doesNotThrowAnyException();
            verify(rolPort).save(any());
        }

        @Test
        @DisplayName("remueve solo el permiso correcto cuando hay múltiples")
        void removeSoloElPermisoCorrecto() {
            Rol rol = rolConPermisos(); // tiene permiso 1 y 2
            when(rolPort.findById(1L)).thenReturn(Optional.of(rol));
            when(rolPort.save(any())).thenAnswer(i -> i.getArgument(0));

            rbacService.removerPermisoDeRol(1L, 1L); // remueve solo el id=1

            verify(rolPort).save(argThat(r ->
                    r.getPermisos().size() == 1 &&
                    r.getPermisos().stream().anyMatch(p -> p.getId().equals(2L))
            ));
        }
    }

    // ── removerRolDeUsuario ────────────────────────────────────────────

    @Nested
    @DisplayName("removerRolDeUsuario()")
    class RemoverRolDeUsuario {

        @Test
        @DisplayName("lanza excepción cuando el usuario no existe")
        void usuarioNoExiste() {
            when(usuarioPort.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> rbacService.removerRolDeUsuario(99L, 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Usuario no encontrado");
        }

        @Test
        @DisplayName("no falla si el rol a remover no estaba asignado al usuario")
        void rolNoAsignado() {
            Usuario u = new Usuario();
            u.setId(1L);
            u.setEmail("karen@test.com");
            u.setActivo(true);
            u.setRoles(new HashSet<>()); // sin roles
            when(usuarioPort.findById(1L)).thenReturn(Optional.of(u));
            when(usuarioPort.save(any())).thenAnswer(i -> i.getArgument(0));

            assertThatCode(() -> rbacService.removerRolDeUsuario(1L, 99L))
                    .doesNotThrowAnyException();
        }
    }

    // ── asignarPermisosARol con múltiples permisos ─────────────────────

    @Nested
    @DisplayName("asignarPermisosARol()")
    class AsignarPermisos {

        @Test
        @DisplayName("agrega múltiples permisos al rol en una sola operación")
        void asignaVariosPermisos() {
            Rol rol = new Rol(1L, "USER", "desc", new HashSet<>());
            Permiso p1 = new Permiso(1L, "PRODUCT_SELECT", "Ver");
            Permiso p2 = new Permiso(2L, "PRODUCT_INSERT", "Insertar");
            when(rolPort.findById(1L)).thenReturn(Optional.of(rol));
            when(permisoPort.findAllByIds(List.of(1L, 2L))).thenReturn(List.of(p1, p2));
            when(rolPort.save(any())).thenAnswer(i -> i.getArgument(0));

            rbacService.asignarPermisosARol(1L, List.of(1L, 2L));

            verify(rolPort).save(argThat(r -> r.getPermisos().size() == 2));
        }

        @Test
        @DisplayName("agrega permisos a un rol que ya tenía permisos")
        void acumulaPermisos() {
            Permiso existente = new Permiso(1L, "PRODUCT_SELECT", "Ver");
            Rol rol = new Rol(1L, "USER", "desc", new HashSet<>(Set.of(existente)));
            Permiso nuevo = new Permiso(2L, "PRODUCT_INSERT", "Insertar");
            when(rolPort.findById(1L)).thenReturn(Optional.of(rol));
            when(permisoPort.findAllByIds(List.of(2L))).thenReturn(List.of(nuevo));
            when(rolPort.save(any())).thenAnswer(i -> i.getArgument(0));

            rbacService.asignarPermisosARol(1L, List.of(2L));

            verify(rolPort).save(argThat(r -> r.getPermisos().size() == 2));
        }
    }
}
