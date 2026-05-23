package com.aerorescue.emergency.infrastructure.adapter.out.persistence;

import com.aerorescue.emergency.infrastructure.adapter.out.persistence.entity.EmergencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface EmergencyJpaRepository extends JpaRepository<EmergencyEntity, UUID> {
    List<EmergencyEntity> findByRegion(String region);
    List<EmergencyEntity> findByOrganizationId(String organizationId);
}
