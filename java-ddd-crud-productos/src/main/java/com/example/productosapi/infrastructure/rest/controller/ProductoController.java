package com.example.productosapi.infrastructure.rest.controller;

import com.example.productosapi.application.usecase.CreateProductoUseCase;
import com.example.productosapi.application.usecase.DeleteProductoUseCase;
import com.example.productosapi.application.usecase.GetProductoUseCase;
import com.example.productosapi.application.usecase.UpdateProductoUseCase;
import com.example.productosapi.domain.model.Producto;
import com.example.productosapi.infrastructure.persistence.mapper.ProductoMapper;
import com.example.productosapi.infrastructure.rest.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para operaciones de Productos
 */
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final CreateProductoUseCase createProductoUseCase;
    private final GetProductoUseCase getProductoUseCase;
    private final UpdateProductoUseCase updateProductoUseCase;
    private final DeleteProductoUseCase deleteProductoUseCase;
    private final ProductoMapper mapper;

    /**
     * Crea un nuevo producto
     */
    @PostMapping
    public ResponseEntity<ProductoResponse> crearProducto(@Valid @RequestBody ProductoRequest request) {
        Producto producto = mapper.toDomain(request);
        Producto creado = createProductoUseCase.execute(producto);
        return new ResponseEntity<>(mapper.toResponse(creado), HttpStatus.CREATED);
    }

    /**
     * Obtiene un producto por su ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> obtenerProducto(@PathVariable UUID id) {
        Producto producto = getProductoUseCase.executeById(id);
        return ResponseEntity.ok(mapper.toResponse(producto));
    }

    /**
     * Obtiene un producto por su código
     */
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<ProductoResponse> obtenerProductoPorCodigo(@PathVariable String codigo) {
        Producto producto = getProductoUseCase.executeByCodigo(codigo);
        return ResponseEntity.ok(mapper.toResponse(producto));
    }

    /**
     * Obtiene todos los productos
     */
    @GetMapping
    public ResponseEntity<List<ProductoResponse>> obtenerTodosLosProductos() {
        List<Producto> productos = getProductoUseCase.executeAll();
        return ResponseEntity.ok(mapper.toResponseList(productos));
    }

    /**
     * Obtiene productos por categoría
     */
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<ProductoResponse>> obtenerProductosPorCategoria(@PathVariable String categoria) {
        List<Producto> productos = getProductoUseCase.executeByCategoria(categoria);
        return ResponseEntity.ok(mapper.toResponseList(productos));
    }

    /**
     * Actualiza un producto existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponse> actualizarProducto(
            @PathVariable UUID id,
            @Valid @RequestBody ProductoUpdateRequest request) {

        Producto actualizado = updateProductoUseCase.execute(
                id,
                request.getNombre(),
                request.getDescripcion(),
                request.getPrecio(),
                request.getStock(),
                request.getCategoria()
        );

        return ResponseEntity.ok(mapper.toResponse(actualizado));
    }

    /**
     * Actualiza el estado de un producto
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<ProductoResponse> actualizarEstadoProducto(
            @PathVariable UUID id,
            @Valid @RequestBody EstadoUpdateRequest request) {

        Producto actualizado = updateProductoUseCase.executeStateUpdate(id, request.getActivo());
        return ResponseEntity.ok(mapper.toResponse(actualizado));
    }

    /**
     * Actualiza el stock de un producto
     */
    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductoResponse> actualizarStockProducto(
            @PathVariable UUID id,
            @Valid @RequestBody StockUpdateRequest request) {

        Producto actualizado = updateProductoUseCase.executeStockUpdate(id, request.getCantidad());
        return ResponseEntity.ok(mapper.toResponse(actualizado));
    }

    /**
     * Elimina un producto
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable UUID id) {
        deleteProductoUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}