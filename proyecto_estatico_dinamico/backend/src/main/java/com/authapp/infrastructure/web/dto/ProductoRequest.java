package com.authapp.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductoRequest {
    @NotBlank
    public String nombre;
    public String descripcion;
    @Positive
    public BigDecimal precio;
    public String categoria;
}
