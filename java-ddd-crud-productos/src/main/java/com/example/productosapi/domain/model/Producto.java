package com.example.productosapi.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad de dominio para representar un Producto.
 * Esta clase sigue los principios de inmutabilidad de DDD.
 */
@Getter
@ToString
@Builder
public class Producto {
    private final UUID id;
    private final String codigo;
    private final String nombre;
    private final String descripcion;
    private final BigDecimal precio;
    private final Integer stock;
    private final String categoria;
    private final Boolean activo;
    private final LocalDateTime fechaCreacion;
    private final LocalDateTime fechaActualizacion;

    /**
     * Método para actualizar un producto con nuevos valores
     */
    public Producto actualizar(String nombre, String descripcion, BigDecimal precio,
                               Integer stock, String categoria) {
        return Producto.builder()
                .id(this.id)
                .codigo(this.codigo)
                .nombre(nombre)
                .descripcion(descripcion)
                .precio(precio)
                .stock(stock)
                .categoria(categoria)
                .activo(this.activo)
                .fechaCreacion(this.fechaCreacion)
                .fechaActualizacion(LocalDateTime.now())
                .build();
    }

    /**
     * Método para cambiar el estado activo del producto
     */
    public Producto cambiarEstado(Boolean activo) {
        return Producto.builder()
                .id(this.id)
                .codigo(this.codigo)
                .nombre(this.nombre)
                .descripcion(this.descripcion)
                .precio(this.precio)
                .stock(this.stock)
                .categoria(this.categoria)
                .activo(activo)
                .fechaCreacion(this.fechaCreacion)
                .fechaActualizacion(LocalDateTime.now())
                .build();
    }

    /**
     * Método para validar si hay suficiente stock
     */
    public boolean tieneStockSuficiente(Integer cantidad) {
        return this.stock >= cantidad;
    }

    /**
     * Método para reducir el stock
     */
    public Producto reducirStock(Integer cantidad) {
        if (!tieneStockSuficiente(cantidad)) {
            throw new IllegalArgumentException("No hay suficiente stock disponible");
        }

        return Producto.builder()
                .id(this.id)
                .codigo(this.codigo)
                .nombre(this.nombre)
                .descripcion(this.descripcion)
                .precio(this.precio)
                .stock(this.stock - cantidad)
                .categoria(this.categoria)
                .activo(this.activo)
                .fechaCreacion(this.fechaCreacion)
                .fechaActualizacion(LocalDateTime.now())
                .build();
    }

    /**
     * Método para aumentar el stock
     */
    public Producto aumentarStock(Integer cantidad) {
        return Producto.builder()
                .id(this.id)
                .codigo(this.codigo)
                .nombre(this.nombre)
                .descripcion(this.descripcion)
                .precio(this.precio)
                .stock(this.stock + cantidad)
                .categoria(this.categoria)
                .activo(this.activo)
                .fechaCreacion(this.fechaCreacion)
                .fechaActualizacion(LocalDateTime.now())
                .build();
    }
}