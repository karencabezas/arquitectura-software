package com.demo.productos.service;

import java.util.List;

import com.demo.productos.model.Producto;

public interface ProductoService {

    List<Producto> listarTodosLosProductos();

    Producto obtenerProductoPorId(Long id);

    Producto guardarProducto(Producto producto);

    void eliminarProducto(Long id);

    List<Producto> buscarProductosPorNombre(String nombre);

    List<Producto> buscarProductosPorPrecioMaximo(Double precioMaximo);

    List<Producto> buscarProductosConStock(Integer stockMinimo);
}