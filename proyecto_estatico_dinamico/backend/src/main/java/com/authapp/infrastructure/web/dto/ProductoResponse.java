package com.authapp.infrastructure.web.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductoResponse {
    public Long id;
    public String nombre;
    public String descripcion;
    public BigDecimal precio;
    public String categoria;
    public Long ownerId;
    public boolean activo;
}
