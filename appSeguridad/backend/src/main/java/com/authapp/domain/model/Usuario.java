package com.authapp.domain.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Entidad de dominio pura.
 * No tiene dependencias de Spring, JPA ni ningún framework.
 * Representa el concepto de Usuario desde la perspectiva del negocio.
 */
public class Usuario {

    private Long id;
    private String email;
    private String passwordHash;
    private String totpSecret;
    private boolean mfaEnabled;
    private boolean activo;
    private Set<Rol> roles;

    public Usuario() {
        this.roles = new HashSet<>();
    }

    public Usuario(Long id, String email, String passwordHash,
                   String totpSecret, boolean mfaEnabled, boolean activo, Set<Rol> roles) {
        this.id           = id;
        this.email        = email;
        this.passwordHash = passwordHash;
        this.totpSecret   = totpSecret;
        this.mfaEnabled   = mfaEnabled;
        this.activo       = activo;
        this.roles        = roles != null ? roles : new HashSet<>();
    }

    // ── Reglas de negocio del dominio ──

    public boolean estaActivo() {
        return this.activo;
    }

    public boolean tieneMfaConfigurado() {
        return this.mfaEnabled && this.totpSecret != null && !this.totpSecret.isBlank();
    }

    public boolean tieneRol(String nombreRol) {
        return this.roles.stream().anyMatch(r -> r.getNombre().equals(nombreRol));
    }

    public Set<String> obtenerNombresRoles() {
        Set<String> nombres = new HashSet<>();
        for (Rol r : roles) nombres.add(r.getNombre());
        return nombres;
    }

    public Set<String> obtenerNombresPermisos() {
        Set<String> permisos = new HashSet<>();
        for (Rol r : roles) permisos.addAll(r.obtenerNombresPermisos());
        return permisos;
    }

    // ── Getters y Setters ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getTotpSecret() { return totpSecret; }
    public void setTotpSecret(String totpSecret) { this.totpSecret = totpSecret; }

    public boolean isMfaEnabled() { return mfaEnabled; }
    public void setMfaEnabled(boolean mfaEnabled) { this.mfaEnabled = mfaEnabled; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public Set<Rol> getRoles() { return roles; }
    public void setRoles(Set<Rol> roles) { this.roles = roles; }
}
