package com.authapp.infrastructure.persistence.adapter;

import com.authapp.domain.model.Producto;
import com.authapp.domain.port.ProductoRepositoryPort;
import com.authapp.infrastructure.persistence.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductoRepositoryAdapter implements ProductoRepositoryPort {

    private final ProductoRepository jpaRepository;

    @Override
    public List<Producto> findAllActivos() {
        return jpaRepository.findByActivoTrue().stream()
                .map(DomainMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Producto> findById(Long id) {
        return jpaRepository.findById(id).map(DomainMapper::toDomain);
    }

    @Override
    public Producto save(Producto producto) {
        var entity = DomainMapper.toEntity(producto);
        var saved  = jpaRepository.save(entity);
        return DomainMapper.toDomain(saved);
    }
}
