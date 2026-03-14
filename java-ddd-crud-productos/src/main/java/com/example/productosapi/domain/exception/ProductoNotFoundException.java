package com.example.productosapi.domain.exception;

import java.util.UUID;

/**
 * Excepción de dominio para cuando no se encuentra un producto
 */
public class ProductoNotFoundException extends BusinessException {

    public ProductoNotFoundException(UUID id) {
        super("No se encontró el producto con ID: " + id);
    }

    public ProductoNotFoundException(String codigo) {
        super("No se encontró el producto con código: " + codigo);
    }
}