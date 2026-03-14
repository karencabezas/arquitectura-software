package com.example.productosapi.infrastructure.rest.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para recibir datos de actualización de estado
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadoUpdateRequest {

    @NotNull(message = "El estado activo es obligatorio")
    private Boolean activo;
}