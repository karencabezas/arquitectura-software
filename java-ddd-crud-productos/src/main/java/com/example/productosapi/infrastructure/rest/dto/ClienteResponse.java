package com.example.productosapi.infrastructure.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;
/**
 *
 * @author kcabezas
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponse {

    private UUID id;
    private String numeroDocumento;
    private String nombre;
    private String apellido;
    private String correo;
    private String telefono;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
