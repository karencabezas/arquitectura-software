package com.example.productosapi.domain.repository;

import com.example.productosapi.domain.model.Cliente;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 *
 * @author kcabezas
 */
public interface ClienteRepository {
    
    Cliente save(Cliente cliente);
    
    Optional<Cliente> findById(UUID id);
    
    Optional<Cliente> findByNumeroDocumento(String numeroDocumento);
    
    List<Cliente> findAll();
    
    void deleteById(UUID id);
    
    boolean existsByNumeroDocumento(String numeroDocumento);
    
}
