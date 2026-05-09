package com.authapp.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Domain Model — Reglas de negocio")
class DomainModelTest {

    // ═══════════════════════════════════════════════════════════════════
    // USUARIO
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("estaActivo() retorna true cuando activo=true")
    void usuarioActivo() {
        Usuario u = new Usuario();
        u.setActivo(true);
        assertThat(u.estaActivo()).isTrue();
    }

    @Test
    @DisplayName("estaActivo() retorna false cuando activo=false")
    void usuarioInactivo() {
        Usuario u = new Usuario();
        u.setActivo(false);
        assertThat(u.estaActivo()).isFalse();
    }

    @Test
    @DisplayName("tieneMfaConfigurado() retorna true cuando mfaEnabled=true y tiene secret")
    void mfaConfigurado() {
        Usuario u = new Usuario();
        u.setMfaEnabled(true);
        u.setTotpSecret("SECRETO123");
        assertThat(u.tieneMfaConfigurado()).isTrue();
    }

    @Test
    @DisplayName("tieneMfaConfigurado() retorna false cuando mfaEnabled=false aunque tenga secret")
    void mfaDeshabilitado() {
        Usuario u = new Usuario();
        u.setMfaEnabled(false);
        u.setTotpSecret("SECRETO123");
        assertThat(u.tieneMfaConfigurado()).isFalse();
    }

    @Test
    @DisplayName("tieneMfaConfigurado() retorna false cuando secret es null")
    void mfaSinSecret() {
        Usuario u = new Usuario();
        u.setMfaEnabled(true);
        u.setTotpSecret(null);
        assertThat(u.tieneMfaConfigurado()).isFalse();
    }

    @Test
    @DisplayName("tieneMfaConfigurado() retorna false cuando secret está en blanco")
    void mfaSecretBlanco() {
        Usuario u = new Usuario();
        u.setMfaEnabled(true);
        u.setTotpSecret("   ");
        assertThat(u.tieneMfaConfigurado()).isFalse();
    }

    @Test
    @DisplayName("tieneRol() retorna true cuando el usuario tiene ese rol")
    void tieneRol() {
        Rol rol = new Rol(1L, "ADMIN", "Administrador", new HashSet<>());
        Usuario u = new Usuario();
        u.setRoles(Set.of(rol));
        assertThat(u.tieneRol("ADMIN")).isTrue();
    }

    @Test
    @DisplayName("tieneRol() retorna false cuando el usuario no tiene ese rol")
    void noTieneRol() {
        Usuario u = new Usuario();
        u.setRoles(new HashSet<>());
        assertThat(u.tieneRol("ADMIN")).isFalse();
    }

    @Test
    @DisplayName("obtenerNombresRoles() retorna los nombres de todos los roles")
    void obtenerNombresRoles() {
        Rol admin = new Rol(1L, "ADMIN", "", new HashSet<>());
        Rol user  = new Rol(2L, "USER",  "", new HashSet<>());
        Usuario u = new Usuario();
        u.setRoles(Set.of(admin, user));
        assertThat(u.obtenerNombresRoles()).containsExactlyInAnyOrder("ADMIN", "USER");
    }

    @Test
    @DisplayName("obtenerNombresPermisos() agrega permisos de todos los roles")
    void obtenerNombresPermisos() {
        Permiso p1 = new Permiso(1L, "PRODUCT_SELECT", "");
        Permiso p2 = new Permiso(2L, "PRODUCT_DELETE", "");
        Rol rol1 = new Rol(1L, "ADMIN", "", Set.of(p1, p2));
        Rol rol2 = new Rol(2L, "USER",  "", Set.of(p1));
        Usuario u = new Usuario();
        u.setRoles(Set.of(rol1, rol2));
        assertThat(u.obtenerNombresPermisos())
                .containsExactlyInAnyOrder("PRODUCT_SELECT", "PRODUCT_DELETE");
    }

    // ═══════════════════════════════════════════════════════════════════
    // PRODUCTO
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("perteneceA() retorna true cuando el ownerId coincide")
    void productoPerteneceAlUsuario() {
        Producto p = new Producto(1L, "Laptop", "", BigDecimal.TEN, "Tech", 5L, true);
        assertThat(p.perteneceA(5L)).isTrue();
    }

    @Test
    @DisplayName("perteneceA() retorna false cuando el ownerId no coincide")
    void productoNoPerteneceAlUsuario() {
        Producto p = new Producto(1L, "Laptop", "", BigDecimal.TEN, "Tech", 5L, true);
        assertThat(p.perteneceA(99L)).isFalse();
    }

    @Test
    @DisplayName("perteneceA() retorna false cuando ownerId es null")
    void productoOwnerIdNull() {
        Producto p = new Producto(1L, "Laptop", "", BigDecimal.TEN, "Tech", null, true);
        assertThat(p.perteneceA(5L)).isFalse();
    }

    // ═══════════════════════════════════════════════════════════════════
    // ROL
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("tienePermiso() retorna true cuando el rol tiene ese permiso")
    void rolTienePermiso() {
        Permiso p = new Permiso(1L, "PRODUCT_DELETE", "Eliminar");
        Rol rol = new Rol(1L, "ADMIN", "", Set.of(p));
        assertThat(rol.tienePermiso("PRODUCT_DELETE")).isTrue();
    }

    @Test
    @DisplayName("tienePermiso() retorna false cuando el rol no tiene ese permiso")
    void rolNoTienePermiso() {
        Rol rol = new Rol(1L, "USER", "", new HashSet<>());
        assertThat(rol.tienePermiso("PRODUCT_DELETE")).isFalse();
    }

    @Test
    @DisplayName("obtenerNombresPermisos() retorna los nombres correctamente")
    void rolObtenerNombresPermisos() {
        Permiso p1 = new Permiso(1L, "PRODUCT_SELECT", "");
        Permiso p2 = new Permiso(2L, "PRODUCT_INSERT", "");
        Rol rol = new Rol(1L, "USER", "", Set.of(p1, p2));
        assertThat(rol.obtenerNombresPermisos()).containsExactlyInAnyOrder("PRODUCT_SELECT", "PRODUCT_INSERT");
    }
}
