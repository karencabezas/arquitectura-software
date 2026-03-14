package com.example.productosapi.domain.event;

import com.example.productosapi.domain.model.Producto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento de dominio para productos
 * Se puede utilizar para implementar Event Sourcing o publicar eventos
 */
@Getter
@AllArgsConstructor
public class ProductoEvent {

    public enum TipoEvento {
        CREADO, ACTUALIZADO, ELIMINADO, ESTADO_CAMBIADO, STOCK_ACTUALIZADO
    }

    private final UUID id;
    private final TipoEvento tipo;
    private final Producto producto;
    private final LocalDateTime timestamp;

    /**
     * Crea un evento de producto creado
     */
    public static ProductoEvent creado(Producto producto) {
        return new ProductoEvent(
                UUID.randomUUID(),
                TipoEvento.CREADO,
                producto,
                LocalDateTime.now()
        );
    }

    /**
     * Crea un evento de producto actualizado
     */
    public static ProductoEvent actualizado(Producto producto) {
        return new ProductoEvent(
                UUID.randomUUID(),
                TipoEvento.ACTUALIZADO,
                producto,
                LocalDateTime.now()
        );
    }

    /**
     * Crea un evento de producto eliminado
     */
    public static ProductoEvent eliminado(Producto producto) {
        return new ProductoEvent(
                UUID.randomUUID(),
                TipoEvento.ELIMINADO,
                producto,
                LocalDateTime.now()
        );
    }

    /**
     * Crea un evento de estado de producto cambiado
     */
    public static ProductoEvent estadoCambiado(Producto producto) {
        return new ProductoEvent(
                UUID.randomUUID(),
                TipoEvento.ESTADO_CAMBIADO,
                producto,
                LocalDateTime.now()
        );
    }

    /**
     * Crea un evento de stock de producto actualizado
     */
    public static ProductoEvent stockActualizado(Producto producto) {
        return new ProductoEvent(
                UUID.randomUUID(),
                TipoEvento.STOCK_ACTUALIZADO,
                producto,
                LocalDateTime.now()
        );
    }
}