package com.demo.productos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.demo.productos.model.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Métodos personalizados de búsqueda
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    @Query("SELECT p FROM Producto p WHERE p.precio <= ?1")
    List<Producto> buscarProductosPorPrecioMaximo(Double precioMaximo);

    List<Producto> findByCantidadGreaterThan(Integer stockMinimo);
}