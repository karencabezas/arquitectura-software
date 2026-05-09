package com.authapp.infrastructure.persistence.repository;

import com.authapp.infrastructure.persistence.entity.RolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RolRepository extends JpaRepository<RolEntity, Long> {
    Optional<RolEntity> findByNombre(String nombre);
    boolean existsByNombre(String nombre);
}
