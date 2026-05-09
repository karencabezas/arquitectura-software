package com.authapp.infrastructure.persistence.repository;

import com.authapp.infrastructure.persistence.entity.PermisoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PermisoRepository extends JpaRepository<PermisoEntity, Long> {
    Optional<PermisoEntity> findByNombre(String nombre);
}
