package com.example.productosapi.infrastructure.persistence.repository;

import com.example.productosapi.infrastructure.persistence.entity.ProductoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para la entidad ProductoEntity
 */
@Repository
public interface ProductoJpaRepository extends JpaRepository<ProductoEntity, UUID> {

    /**
     * Busca un producto por su código
     */
    Optional<ProductoEntity> findByCodigo(String codigo);

    /**
     * Busca productos por categoría
     */
    List<ProductoEntity> findByCategoria(String categoria);

    /**
     * Verifica si existe un producto con el código proporcionado
     */
    boolean existsByCodigo(String codigo);
}