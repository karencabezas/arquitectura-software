package com.example.productosapi.infrastructure.persistence.repository;

import com.example.productosapi.domain.model.Cliente;
import com.example.productosapi.domain.repository.ClienteRepository;
import com.example.productosapi.infrastructure.persistence.mapper.ClienteMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 *
 * @author kcabezas
 */
@Component
@RequiredArgsConstructor
public class ClienteRepositoryAdapter implements ClienteRepository {
    
    private final ClienteJpaRepository jpaRepository;
    private final ClienteMapper mapper;

    @Override
    public Cliente save(Cliente cliente) {
        var entity = mapper.toEntity(cliente);
        var savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Cliente> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Cliente> findByNumeroDocumento(String numeroDocumento) {
        return jpaRepository.findByNumeroDocumento(numeroDocumento)
                .map(mapper::toDomain);
    }

    @Override
    public List<Cliente> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByNumeroDocumento(String numeroDocumento) {
        return jpaRepository.existsByNumeroDocumento(numeroDocumento);
    }
    
}
