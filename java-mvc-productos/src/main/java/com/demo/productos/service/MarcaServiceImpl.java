package com.demo.productos.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.demo.productos.model.Marca;
import com.demo.productos.repository.MarcaRepository;

@Service
public class MarcaServiceImpl implements MarcaService {
    @Autowired
    private MarcaRepository marcaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Marca> listarTodasLasMarcas() {
        return marcaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Marca obtenerMarcaPorId(Long id) {
        return marcaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Marca no encontrada con ID: " + id));
    }

    @Override
    @Transactional
    public Marca guardarMarca(Marca marca) {
        return marcaRepository.save(marca);
    }

    @Override
    @Transactional
    public void eliminarMarca(Long id) {
        marcaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Marca> buscarMarcasPorNombre(String nombre) {
        return marcaRepository.findByNombreContainingIgnoreCase(nombre);
    }
}
