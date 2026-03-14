package com.example.productosapi.domain.repository;

import com.example.productosapi.domain.model.Producto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interfaz del repositorio de dominio para Productos.
 * Define las operaciones de persistencia sin detalles de implementación.
 */
public interface ProductoRepository {

    /**
     * Guarda un nuevo producto o actualiza uno existente
     * @param producto el producto a guardar
     * @return el producto guardado
     */
    Producto save(Producto producto);

    /**
     * Busca un producto por su ID
     * @param id el ID del producto
     * @return un Optional con el producto si existe
     */
    Optional<Producto> findById(UUID id);

    /**
     * Busca un producto por su código
     * @param codigo el código del producto
     * @return un Optional con el producto si existe
     */
    Optional<Producto> findByCodigo(String codigo);

    /**
     * Obtiene todos los productos
     * @return lista de productos
     */
    List<Producto> findAll();

    /**
     * Busca productos por categoría
     * @param categoria la categoría a buscar
     * @return lista de productos que pertenecen a la categoría
     */
    List<Producto> findByCategoria(String categoria);

    /**
     * Elimina un producto por su ID
     * @param id el ID del producto a eliminar
     */
    void deleteById(UUID id);

    /**
     * Verifica si existe un producto con el código proporcionado
     * @param codigo el código a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByCodigo(String codigo);
}