package com.authapp.domain.model;

import java.math.BigDecimal;

/**
 * Entidad de dominio pura.
 * Recurso protegido por ABAC — las reglas de acceso
 * se evalúan en la capa application usando ownerId.
 */
public class Producto {

    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private String categoria;
    private Long ownerId;
    private boolean activo;

    public Producto() {}

    public Producto(Long id, String nombre, String descripcion,
                    BigDecimal precio, String categoria, Long ownerId, boolean activo) {
        this.id          = id;
        this.nombre      = nombre;
        this.descripcion = descripcion;
        this.precio      = precio;
        this.categoria   = categoria;
        this.ownerId     = ownerId;
        this.activo      = activo;
    }

    // ── Reglas de negocio del dominio ──

    /**
     * Un usuario puede modificar este producto si es su dueño
     * o si tiene rol ADMIN (evaluado en application con los permisos).
     */
    public boolean perteneceA(Long usuarioId) {
        return this.ownerId != null && this.ownerId.equals(usuarioId);
    }

    // ── Getters y Setters ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
