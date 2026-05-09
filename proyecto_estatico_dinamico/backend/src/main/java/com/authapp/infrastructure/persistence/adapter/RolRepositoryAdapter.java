package com.authapp.infrastructure.persistence.adapter;

import com.authapp.domain.model.Rol;
import com.authapp.domain.port.RolRepositoryPort;
import com.authapp.infrastructure.persistence.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RolRepositoryAdapter implements RolRepositoryPort {

    private final RolRepository jpaRepository;

    @Override
    public List<Rol> findAll() {
        return jpaRepository.findAll().stream()
                .map(DomainMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Rol> findById(Long id) {
        return jpaRepository.findById(id).map(DomainMapper::toDomain);
    }

    @Override
    public Optional<Rol> findByNombre(String nombre) {
        return jpaRepository.findByNombre(nombre).map(DomainMapper::toDomain);
    }

    @Override
    public boolean existsByNombre(String nombre) {
        return jpaRepository.existsByNombre(nombre);
    }

    @Override
    public Rol save(Rol rol) {
        var entity = DomainMapper.toEntity(rol);
        var saved  = jpaRepository.save(entity);
        return DomainMapper.toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
