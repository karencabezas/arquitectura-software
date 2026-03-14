package com.example.productosapi.infrastructure.persistence.repository;

import com.example.productosapi.infrastructure.persistence.entity.ClienteEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author kcabezas
 */
@Repository
public interface ClienteJpaRepository extends JpaRepository<ClienteEntity, UUID>{
    
    Optional<ClienteEntity> findByNumeroDocumento(String codigo);
    
    boolean existsByNumeroDocumento(String codigo);

}
