package com.example.productosapi.infrastructure.persistence.repository;

import com.example.productosapi.domain.model.Producto;
import com.example.productosapi.domain.repository.ProductoRepository;
import com.example.productosapi.infrastructure.persistence.mapper.ProductoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación del repositorio de dominio que utiliza el repositorio JPA
 * Implementa el patrón Adapter para conectar el dominio con la infraestructura
 */
@Component
@RequiredArgsConstructor
public class ProductoRepositoryAdapter implements ProductoRepository {

    private final ProductoJpaRepository jpaRepository;
    private final ProductoMapper mapper;

    @Override
    public Producto save(Producto producto) {
        var entity = mapper.toEntity(producto);
        var savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Producto> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Producto> findByCodigo(String codigo) {
        return jpaRepository.findByCodigo(codigo)
                .map(mapper::toDomain);
    }

    @Override
    public List<Producto> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Producto> findByCategoria(String categoria) {
        return jpaRepository.findByCategoria(categoria).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByCodigo(String codigo) {
        return jpaRepository.existsByCodigo(codigo);
    }
}