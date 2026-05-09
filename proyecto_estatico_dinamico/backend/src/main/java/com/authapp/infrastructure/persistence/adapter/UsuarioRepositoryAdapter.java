package com.authapp.infrastructure.persistence.adapter;

import com.authapp.domain.model.Usuario;
import com.authapp.domain.port.UsuarioRepositoryPort;
import com.authapp.infrastructure.persistence.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ADAPTER de salida (driven adapter).
 *
 * Implementa el port definido por el dominio usando Spring Data JPA.
 * La capa application no sabe que esto existe — solo conoce el port.
 *
 * Patrón: Ports & Adapters (Hexagonal Architecture)
 */
@Component
@RequiredArgsConstructor
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {

    private final UsuarioRepository jpaRepository;

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(DomainMapper::toDomain);
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        return jpaRepository.findById(id).map(DomainMapper::toDomain);
    }

    @Override
    public List<Usuario> findAll() {
        return jpaRepository.findAll().stream()
                .map(DomainMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public Usuario save(Usuario usuario) {
        var entity = DomainMapper.toEntity(usuario);
        var saved  = jpaRepository.save(entity);
        return DomainMapper.toDomain(saved);
    }
}
