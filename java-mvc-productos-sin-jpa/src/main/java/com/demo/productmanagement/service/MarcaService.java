package com.demo.productmanagement.service;

import com.demo.productmanagement.model.Marca;

import java.util.List;
import java.util.Optional;

public interface MarcaService {
    List<Marca> getAllMarcas();

    Optional<Marca> getMarcaById(Long id);

    Marca saveMarca(Marca marca);

    boolean updateMarca(Marca marca);

    boolean deleteMarca(Long id);
}
