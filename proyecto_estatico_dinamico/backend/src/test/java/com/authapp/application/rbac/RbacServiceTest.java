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
@DisplayName("RbacService")
class RbacServiceTest {

    @Mock RolRepositoryPort     rolPort;
    @Mock PermisoRepositoryPort permisoPort;
    @Mock UsuarioRepositoryPort usuarioPort;

    @InjectMocks RbacService rbacService;

    // ── Helpers ──────────────────────────────────────────────────────────

    private Rol rolAdmin() {
        return new Rol(1L, "ADMIN", "Administrador", new HashSet<>());
    }

    private Usuario usuarioBase() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setEmail("karen@test.com");
        u.setActivo(true);
        u.setRoles(new HashSet<>());
        return u;
    }

    // ═══════════════════════════════════════════════════════════════════
    // ROLES
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Gestión de roles")
    class Roles {

        @Test
        @DisplayName("listarRoles() delega al port")
        void listarRoles() {
            when(rolPort.findAll()).thenReturn(List.of(rolAdmin()));

            List<Rol> result = rbacService.listarRoles();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getNombre()).isEqualTo("ADMIN");
        }

        @Test
        @DisplayName("crearRol() guarda el rol cuando el nombre no existe")
        void crearRolExitoso() {
            when(rolPort.existsByNombre("EDITOR")).thenReturn(false);
            when(rolPort.save(any())).thenAnswer(i -> i.getArgument(0));

            Rol result = rbacService.crearRol("EDITOR", "Editor de contenido");

            assertThat(result.getNombre()).isEqualTo("EDITOR");
            verify(rolPort).save(any());
        }

        @Test
        @DisplayName("crearRol() lanza excepción cuando el nombre ya existe")
        void crearRolDuplicado() {
            when(rolPort.existsByNombre("ADMIN")).thenReturn(true);

            assertThatThrownBy(() -> rbacService.crearRol("ADMIN", "desc"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Ya existe un rol con ese nombre");
        }

        @Test
        @DisplayName("actualizarRol() cambia nombre y descripción")
        void actualizarRol() {
            Rol rol = rolAdmin();
            when(rolPort.findById(1L)).thenReturn(Optional.of(rol));
            when(rolPort.save(any())).thenAnswer(i -> i.getArgument(0));

            Rol result = rbacService.actualizarRol(1L, "SUPERADMIN", "Super administrador");

            assertThat(result.getNombre()).isEqualTo("SUPERADMIN");
            assertThat(result.getDescripcion()).isEqualTo("Super administrador");
        }

        @Test
        @DisplayName("actualizarRol() lanza excepción cuando el rol no existe")
        void actualizarRolNoExiste() {
            when(rolPort.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> rbacService.actualizarRol(99L, "X", "Y"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Rol no encontrado");
        }

        @Test
        @DisplayName("eliminarRol() delega al port")
        void eliminarRol() {
            rbacService.eliminarRol(1L);
            verify(rolPort).deleteById(1L);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // PERMISOS
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Gestión de permisos")
    class Permisos {

        @Test
        @DisplayName("listarPermisos() delega al port")
        void listarPermisos() {
            Permiso p = new Permiso(1L, "PRODUCT_SELECT", "Ver productos");
            when(permisoPort.findAll()).thenReturn(List.of(p));

            List<Permiso> result = rbacService.listarPermisos();

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("asignarPermisosARol() agrega permisos al rol")
        void asignarPermisos() {
            Rol rol = rolAdmin();
            Permiso p = new Permiso(1L, "PRODUCT_SELECT", "Ver");
            when(rolPort.findById(1L)).thenReturn(Optional.of(rol));
            when(permisoPort.findAllByIds(List.of(1L))).thenReturn(List.of(p));
            when(rolPort.save(any())).thenAnswer(i -> i.getArgument(0));

            rbacService.asignarPermisosARol(1L, List.of(1L));

            verify(rolPort).save(argThat(r -> r.getPermisos().contains(p)));
        }

        @Test
        @DisplayName("asignarPermisosARol() lanza excepción cuando el rol no existe")
        void asignarPermisosRolNoExiste() {
            when(rolPort.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> rbacService.asignarPermisosARol(99L, List.of(1L)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Rol no encontrado");
        }

        @Test
        @DisplayName("removerPermisoDeRol() elimina el permiso del rol")
        void removerPermiso() {
            Permiso p = new Permiso(1L, "PRODUCT_DELETE", "Eliminar");
            Rol rol = new Rol(1L, "USER", "Usuario", new HashSet<>(Set.of(p)));
            when(rolPort.findById(1L)).thenReturn(Optional.of(rol));
            when(rolPort.save(any())).thenAnswer(i -> i.getArgument(0));

            rbacService.removerPermisoDeRol(1L, 1L);

            verify(rolPort).save(argThat(r -> r.getPermisos().isEmpty()));
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // USUARIOS
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Asignación de roles a usuarios")
    class Usuarios {

        @Test
        @DisplayName("listarUsuarios() delega al port")
        void listarUsuarios() {
            when(usuarioPort.findAll()).thenReturn(List.of(usuarioBase()));

            List<Usuario> result = rbacService.listarUsuarios();

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("asignarRolAUsuario() agrega el rol al usuario")
        void asignarRol() {
            Usuario u = usuarioBase();
            Rol rol = rolAdmin();
            when(usuarioPort.findById(1L)).thenReturn(Optional.of(u));
            when(rolPort.findById(1L)).thenReturn(Optional.of(rol));
            when(usuarioPort.save(any())).thenAnswer(i -> i.getArgument(0));

            rbacService.asignarRolAUsuario(1L, 1L);

            verify(usuarioPort).save(argThat(saved -> saved.getRoles().contains(rol)));
        }

        @Test
        @DisplayName("asignarRolAUsuario() lanza excepción cuando usuario no existe")
        void asignarRolUsuarioNoExiste() {
            when(usuarioPort.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> rbacService.asignarRolAUsuario(99L, 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Usuario no encontrado");
        }

        @Test
        @DisplayName("asignarRolAUsuario() lanza excepción cuando rol no existe")
        void asignarRolRolNoExiste() {
            when(usuarioPort.findById(1L)).thenReturn(Optional.of(usuarioBase()));
            when(rolPort.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> rbacService.asignarRolAUsuario(1L, 99L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Rol no encontrado");
        }

        @Test
        @DisplayName("removerRolDeUsuario() quita el rol del usuario")
        void removerRol() {
            Rol rol = rolAdmin();
            Usuario u = usuarioBase();
            u.getRoles().add(rol);
            when(usuarioPort.findById(1L)).thenReturn(Optional.of(u));
            when(usuarioPort.save(any())).thenAnswer(i -> i.getArgument(0));

            rbacService.removerRolDeUsuario(1L, 1L);

            verify(usuarioPort).save(argThat(saved -> saved.getRoles().isEmpty()));
        }
    }
}
