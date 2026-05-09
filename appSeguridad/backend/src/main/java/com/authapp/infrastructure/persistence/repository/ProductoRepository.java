package com.authapp.infrastructure.persistence.repository;

import com.authapp.infrastructure.persistence.entity.ProductoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductoRepository extends JpaRepository<ProductoEntity, Long> {
    List<ProductoEntity> findByActivoTrue();
    List<ProductoEntity> findByOwnerIdAndActivoTrue(Long ownerId);
}
