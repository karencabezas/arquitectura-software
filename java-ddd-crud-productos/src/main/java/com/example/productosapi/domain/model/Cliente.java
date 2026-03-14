package com.example.productosapi.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 *
 * @author kcabezas
 */
@Getter
@ToString
@Builder
public class Cliente {
    private final UUID id;
    private final String numeroDocumento;
    private final String nombre;
    private final String apellido;
    private final String correo;
    private final String telefono;
    private final Boolean activo;
    private final LocalDateTime fechaCreacion;
    private final LocalDateTime fechaActualizacion;
    
    public Cliente actualizar(String nombre, String apellido,String correo,String telefono) {
        return Cliente.builder()
                .id(this.id)
                .numeroDocumento(this.numeroDocumento)
                .nombre(nombre)
                .apellido(apellido)
                .correo(correo)
                .telefono(telefono)
                .activo(this.activo)
                .fechaCreacion(this.fechaCreacion)
                .fechaActualizacion(LocalDateTime.now())
                .build();
    }

    /**
     * Método para cambiar el estado activo del producto
     */
    public Cliente cambiarEstado(Boolean activo) {
        return Cliente.builder()
                .id(this.id)
                .numeroDocumento(this.numeroDocumento)
                .nombre(this.nombre)
                .apellido(this.apellido)
                .correo(this.correo)
                .telefono(this.telefono)
                .activo(activo)
                .fechaCreacion(this.fechaCreacion)
                .fechaActualizacion(LocalDateTime.now())
                .build();
    }
    
    
}
