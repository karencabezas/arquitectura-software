package com.example.productosapi.application.usecase;

import com.example.productosapi.domain.model.Producto;
import com.example.productosapi.domain.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso para obtener productos
 */
@Component
@RequiredArgsConstructor
public class GetProductoUseCase {

    private final ProductoService productoService;

    /**
     * Obtiene un producto por su ID
     * @param id el ID del producto
     * @return el producto encontrado
     */
    public Producto executeById(UUID id) {
        return productoService.obtenerProductoPorId(id);
    }

    /**
     * Obtiene un producto por su código
     * @param codigo el código del producto
     * @return el producto encontrado
     */
    public Producto executeByCodigo(String codigo) {
        return productoService.obtenerProductoPorCodigo(codigo);
    }

    /**
     * Obtiene todos los productos
     * @return lista de todos los productos
     */
    public List<Producto> executeAll() {
        return productoService.obtenerTodosLosProductos();
    }

    /**
     * Obtiene productos por categoría
     * @param categoria la categoría a buscar
     * @return lista de productos de la categoría
     */
    public List<Producto> executeByCategoria(String categoria) {
        return productoService.obtenerProductosPorCategoria(categoria);
    }
}