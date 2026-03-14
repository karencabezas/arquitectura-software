package com.example.productosapi.domain.service;

import com.example.productosapi.domain.exception.BusinessException;
import com.example.productosapi.domain.exception.ProductoNotFoundException;
import com.example.productosapi.domain.model.Producto;
import com.example.productosapi.domain.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Implementación del servicio de dominio para Productos
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;

    @Override
    public Producto crearProducto(Producto producto) {
        if (productoRepository.existsByCodigo(producto.getCodigo())) {
            throw new BusinessException("Ya existe un producto con el código: " + producto.getCodigo());
        }

        return productoRepository.save(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public Producto obtenerProductoPorId(UUID id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Producto obtenerProductoPorCodigo(String codigo) {
        return productoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ProductoNotFoundException(codigo));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> obtenerTodosLosProductos() {
        return productoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> obtenerProductosPorCategoria(String categoria) {
        return productoRepository.findByCategoria(categoria);
    }

    @Override
    public Producto actualizarProducto(UUID id, String nombre, String descripcion,
                                       BigDecimal precio, Integer stock, String categoria) {
        Producto productoExistente = obtenerProductoPorId(id);

        Producto productoActualizado = productoExistente.actualizar(
                nombre,
                descripcion,
                precio,
                stock,
                categoria
        );

        return productoRepository.save(productoActualizado);
    }

    @Override
    public Producto actualizarEstadoProducto(UUID id, Boolean activo) {
        Producto productoExistente = obtenerProductoPorId(id);

        Producto productoActualizado = productoExistente.cambiarEstado(activo);

        return productoRepository.save(productoActualizado);
    }

    @Override
    public void eliminarProducto(UUID id) {
        if (!productoRepository.findById(id).isPresent()) {
            throw new ProductoNotFoundException(id);
        }

        productoRepository.deleteById(id);
    }

    @Override
    public Producto actualizarStock(UUID id, Integer cantidad) {
        Producto producto = obtenerProductoPorId(id);
        Producto productoActualizado;

        if (cantidad >= 0) {
            productoActualizado = producto.aumentarStock(cantidad);
        } else {
            try {
                productoActualizado = producto.reducirStock(Math.abs(cantidad));
            } catch (IllegalArgumentException e) {
                throw new BusinessException("No hay suficiente stock disponible para el producto: " + id);
            }
        }

        return productoRepository.save(productoActualizado);
    }
}