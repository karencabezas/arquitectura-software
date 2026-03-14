package com.example.productosapi.application.usecase;

import com.example.productosapi.domain.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Caso de uso para eliminar un producto
 */
@Component
@RequiredArgsConstructor
public class DeleteProductoUseCase {

    private final ProductoService productoService;

    /**
     * Ejecuta el caso de uso para eliminar un producto
     * @param id ID del producto a eliminar
     */
    public void execute(UUID id) {
        productoService.eliminarProducto(id);
    }
}