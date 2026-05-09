package com.authapp.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AppUserPrincipal")
class AppUserPrincipalTest {

    private AppUserPrincipal principal(List<String> roles, List<String> permissions) {
        return new AppUserPrincipal(1L, "karen@test.com", roles, permissions);
    }

    @Test
    @DisplayName("getters retornan los valores asignados en el constructor")
    void getters() {
        AppUserPrincipal p = principal(List.of("ADMIN"), List.of("PRODUCT_SELECT"));
        assertThat(p.getId()).isEqualTo(1L);
        assertThat(p.getEmail()).isEqualTo("karen@test.com");
        assertThat(p.getRoles()).containsExactly("ADMIN");
        assertThat(p.getPermissions()).containsExactly("PRODUCT_SELECT");
    }

    @Test
    @DisplayName("hasPermission() retorna true cuando el permiso existe")
    void tienePermiso() {
        AppUserPrincipal p = principal(List.of(), List.of("PRODUCT_DELETE", "PRODUCT_SELECT"));
        assertThat(p.hasPermission("PRODUCT_DELETE")).isTrue();
    }

    @Test
    @DisplayName("hasPermission() retorna false cuando el permiso no existe")
    void noTienePermiso() {
        AppUserPrincipal p = principal(List.of(), List.of("PRODUCT_SELECT"));
        assertThat(p.hasPermission("PRODUCT_DELETE")).isFalse();
    }

    @Test
    @DisplayName("hasPermission() retorna false cuando permissions es null")
    void hasPermissionConNull() {
        AppUserPrincipal p = new AppUserPrincipal(1L, "karen@test.com", List.of(), null);
        assertThat(p.hasPermission("PRODUCT_DELETE")).isFalse();
    }

    @Test
    @DisplayName("isAdmin() retorna true cuando el rol ADMIN está presente")
    void esAdmin() {
        AppUserPrincipal p = principal(List.of("ADMIN", "USER"), List.of());
        assertThat(p.isAdmin()).isTrue();
    }

    @Test
    @DisplayName("isAdmin() retorna false cuando no tiene rol ADMIN")
    void noEsAdmin() {
        AppUserPrincipal p = principal(List.of("USER"), List.of());
        assertThat(p.isAdmin()).isFalse();
    }

    @Test
    @DisplayName("isAdmin() retorna false cuando roles es null")
    void isAdminConNull() {
        AppUserPrincipal p = new AppUserPrincipal(1L, "karen@test.com", null, List.of());
        assertThat(p.isAdmin()).isFalse();
    }
}
