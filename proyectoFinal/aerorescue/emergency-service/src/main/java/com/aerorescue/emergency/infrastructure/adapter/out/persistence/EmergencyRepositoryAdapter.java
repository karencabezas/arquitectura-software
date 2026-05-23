package com.aerorescue.emergency.infrastructure.adapter.out.persistence;

import com.aerorescue.emergency.domain.model.*;
import com.aerorescue.emergency.domain.port.out.EmergencyRepository;
import com.aerorescue.emergency.infrastructure.adapter.out.persistence.entity.EmergencyEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EmergencyRepositoryAdapter implements EmergencyRepository {

    private final EmergencyJpaRepository jpa;

    @Override
    public Emergency save(Emergency e) {
        return toDomain(jpa.save(toEntity(e)));
    }

    @Override
    public Optional<Emergency> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<Emergency> findAll() {
        return jpa.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Emergency> findByRegion(String region) {
        return jpa.findByRegion(region).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Emergency> findByOrganizationId(String orgId) {
        return jpa.findByOrganizationId(orgId).stream().map(this::toDomain).toList();
    }

    private Emergency toDomain(EmergencyEntity e) {
        return Emergency.builder()
            .id(e.getId())
            .status(e.getStatus())
            .priority(e.getPriority())
            .type(e.getType())
            .location(Location.builder()
                .address(e.getAddress()).region(e.getRegion())
                .lat(e.getLat()).lng(e.getLng()).build())
            .description(e.getDescription())
            .reportedBy(e.getReportedBy())
            .organizationId(e.getOrganizationId())
            .createdAt(e.getCreatedAt())
            .updatedAt(e.getUpdatedAt())
            .build();
    }

    private EmergencyEntity toEntity(Emergency e) {
        return EmergencyEntity.builder()
            .id(e.getId())
            .status(e.getStatus())
            .priority(e.getPriority())
            .type(e.getType())
            .address(e.getLocation().getAddress())
            .region(e.getLocation().getRegion())
            .lat(e.getLocation().getLat())
            .lng(e.getLocation().getLng())
            .description(e.getDescription())
            .reportedBy(e.getReportedBy())
            .organizationId(e.getOrganizationId())
            .createdAt(e.getCreatedAt())
            .updatedAt(e.getUpdatedAt())
            .build();
    }
}
