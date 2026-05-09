package com.authapp.infrastructure.persistence.adapter;

import com.authapp.domain.model.Permiso;
import com.authapp.domain.port.PermisoRepositoryPort;
import com.authapp.infrastructure.persistence.repository.PermisoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PermisoRepositoryAdapter implements PermisoRepositoryPort {

    private final PermisoRepository jpaRepository;

    @Override
    public List<Permiso> findAll() {
        return jpaRepository.findAll().stream()
                .map(DomainMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Permiso> findById(Long id) {
        return jpaRepository.findById(id).map(DomainMapper::toDomain);
    }

    @Override
    public List<Permiso> findAllByIds(List<Long> ids) {
        return jpaRepository.findAllById(ids).stream()
                .map(DomainMapper::toDomain)
                .collect(Collectors.toList());
    }
}
