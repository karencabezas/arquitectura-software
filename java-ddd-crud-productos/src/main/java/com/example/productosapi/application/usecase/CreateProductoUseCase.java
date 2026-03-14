package com.example.productosapi.application.usecase;

import com.example.productosapi.domain.model.Producto;
import com.example.productosapi.domain.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Caso de uso para crear un producto
 */
@Component
@RequiredArgsConstructor
public class CreateProductoUseCase {

    private final ProductoService productoService;

    /**
     * Ejecuta el caso de uso
     * @param producto el producto a crear
     * @return el producto creado
     */
    public Producto execute(Producto producto) {
        return productoService.crearProducto(producto);
    }
}