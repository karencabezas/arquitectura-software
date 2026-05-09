package com.authapp.domain.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Entidad de dominio pura.
 * Un Rol agrupa permisos y se asigna a usuarios (RBAC).
 */
public class Rol {

    private Long id;
    private String nombre;
    private String descripcion;
    private Set<Permiso> permisos;

    public Rol() {
        this.permisos = new HashSet<>();
    }

    public Rol(Long id, String nombre, String descripcion, Set<Permiso> permisos) {
        this.id          = id;
        this.nombre      = nombre;
        this.descripcion = descripcion;
        this.permisos    = permisos != null ? permisos : new HashSet<>();
    }

    // ── Reglas de negocio del dominio ──

    public boolean tienePermiso(String nombrePermiso) {
        return this.permisos.stream().anyMatch(p -> p.getNombre().equals(nombrePermiso));
    }

    public Set<String> obtenerNombresPermisos() {
        Set<String> nombres = new HashSet<>();
        for (Permiso p : permisos) nombres.add(p.getNombre());
        return nombres;
    }

    // ── Getters y Setters ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Set<Permiso> getPermisos() { return permisos; }
    public void setPermisos(Set<Permiso> permisos) { this.permisos = permisos; }
}
