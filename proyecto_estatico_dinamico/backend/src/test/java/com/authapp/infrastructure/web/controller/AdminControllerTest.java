package com.authapp.infrastructure.web.controller;

import com.authapp.application.rbac.RbacService;
import com.authapp.application.rbac.UsuarioAdminService;
import com.authapp.domain.model.Permiso;
import com.authapp.domain.model.Rol;
import com.authapp.domain.model.Usuario;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import({com.authapp.infrastructure.security.SecurityConfig.class,
         com.authapp.infrastructure.security.JwtAuthFilter.class,
         com.authapp.infrastructure.security.JwtService.class})
@DisplayName("AdminController")
class AdminControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean RbacService rbacService;
    @MockBean UsuarioAdminService usuarioAdminService;

    private Rol rolEjemplo() {
        return new Rol(1L, "ADMIN", "Administrador", new HashSet<>());
    }

    private Usuario usuarioEjemplo() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setEmail("karen@test.com");
        u.setActivo(true);
        u.setRoles(new HashSet<>());
        return u;
    }

    // ── Roles ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Roles CRUD")
    class Roles {

        @Test
        @DisplayName("GET /roles retorna lista de roles")
        void listarRoles() throws Exception {
            when(rbacService.listarRoles()).thenReturn(List.of(rolEjemplo()));

            mockMvc.perform(get("/api/admin/roles")
                    .with(user("karen@test.com").roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].nombre").value("ADMIN"));
        }

        @Test
        @DisplayName("POST /roles crea un nuevo rol y retorna 200")
        void crearRol() throws Exception {
            when(rbacService.crearRol(anyString(), anyString())).thenReturn(rolEjemplo());

            mockMvc.perform(post("/api/admin/roles")
                    .with(csrf())
                    .with(user("karen@test.com").roles("ADMIN"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"nombre\":\"ADMIN\",\"descripcion\":\"Administrador\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nombre").value("ADMIN"));
        }

        @Test
        @DisplayName("PUT /roles/{id} actualiza el rol")
        void actualizarRol() throws Exception {
            when(rbacService.actualizarRol(anyLong(), anyString(), anyString())).thenReturn(rolEjemplo());

            mockMvc.perform(put("/api/admin/roles/1")
                    .with(csrf())
                    .with(user("karen@test.com").roles("ADMIN"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"nombre\":\"ADMIN\",\"descripcion\":\"Administrador\"}"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("DELETE /roles/{id} elimina el rol y retorna mensaje")
        void eliminarRol() throws Exception {
            doNothing().when(rbacService).eliminarRol(anyLong());

            mockMvc.perform(delete("/api/admin/roles/1")
                    .with(csrf())
                    .with(user("karen@test.com").roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Rol eliminado"));
        }
    }

    // ── Permisos ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("Permisos")
    class Permisos {

        @Test
        @DisplayName("GET /permisos retorna lista de permisos")
        void listarPermisos() throws Exception {
            when(rbacService.listarPermisos()).thenReturn(
                    List.of(new Permiso(1L, "PRODUCT_SELECT", "Ver")));

            mockMvc.perform(get("/api/admin/permisos")
                    .with(user("karen@test.com").roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].nombre").value("PRODUCT_SELECT"));
        }

        @Test
        @DisplayName("POST /roles/{rolId}/permisos asigna permisos al rol")
        void asignarPermisos() throws Exception {
            doNothing().when(rbacService).asignarPermisosARol(anyLong(), anyList());

            mockMvc.perform(post("/api/admin/roles/1/permisos")
                    .with(csrf())
                    .with(user("karen@test.com").roles("ADMIN"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"permisoIds\":[1,2]}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Permisos asignados"));
        }

        @Test
        @DisplayName("DELETE /roles/{rolId}/permisos/{permisoId} remueve el permiso del rol")
        void removerPermiso() throws Exception {
            doNothing().when(rbacService).removerPermisoDeRol(anyLong(), anyLong());

            mockMvc.perform(delete("/api/admin/roles/1/permisos/2")
                    .with(csrf())
                    .with(user("karen@test.com").roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Permiso removido"));
        }
    }

    // ── Usuarios ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("Usuarios admin")
    class Usuarios {

        @Test
        @DisplayName("GET /usuarios retorna lista de usuarios")
        void listarUsuarios() throws Exception {
            when(rbacService.listarUsuarios()).thenReturn(List.of(usuarioEjemplo()));

            mockMvc.perform(get("/api/admin/usuarios")
                    .with(user("karen@test.com").roles("ADMIN")))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST /usuarios crea un usuario y retorna 200")
        void crearUsuario() throws Exception {
            when(usuarioAdminService.crearUsuario(anyString(), anyString(), anyBoolean()))
                    .thenReturn(usuarioEjemplo());

            mockMvc.perform(post("/api/admin/usuarios")
                    .with(csrf())
                    .with(user("karen@test.com").roles("ADMIN"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"nuevo@test.com\",\"password\":\"pass123\",\"activo\":true}"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("PUT /usuarios/{id} actualiza un usuario")
        void actualizarUsuario() throws Exception {
            when(usuarioAdminService.actualizarUsuario(anyLong(), anyString(), anyBoolean(), any()))
                    .thenReturn(usuarioEjemplo());

            mockMvc.perform(put("/api/admin/usuarios/1")
                    .with(csrf())
                    .with(user("karen@test.com").roles("ADMIN"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"karen@test.com\",\"activo\":true,\"password\":\"\"}"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST /usuarios/{usuarioId}/roles/{rolId} asigna rol al usuario")
        void asignarRol() throws Exception {
            doNothing().when(rbacService).asignarRolAUsuario(anyLong(), anyLong());

            mockMvc.perform(post("/api/admin/usuarios/1/roles/2")
                    .with(csrf())
                    .with(user("karen@test.com").roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Rol asignado"));
        }

        @Test
        @DisplayName("DELETE /usuarios/{usuarioId}/roles/{rolId} remueve rol del usuario")
        void removerRol() throws Exception {
            doNothing().when(rbacService).removerRolDeUsuario(anyLong(), anyLong());

            mockMvc.perform(delete("/api/admin/usuarios/1/roles/2")
                    .with(csrf())
                    .with(user("karen@test.com").roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Rol removido"));
        }
    }
}
