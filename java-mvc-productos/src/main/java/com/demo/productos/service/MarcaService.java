package com.demo.productos.service;

import java.util.List;

import com.demo.productos.model.Marca;

public interface MarcaService {

    List<Marca> listarTodasLasMarcas();

    Marca obtenerMarcaPorId(Long id);

    Marca guardarMarca(Marca marca);

    void eliminarMarca(Long id);

    List<Marca> buscarMarcasPorNombre(String nombre);

}
