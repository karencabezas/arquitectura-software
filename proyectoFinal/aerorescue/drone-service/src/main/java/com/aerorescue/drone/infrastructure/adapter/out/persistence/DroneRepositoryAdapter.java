package com.aerorescue.drone.infrastructure.adapter.out.persistence;

import com.aerorescue.drone.domain.model.*;
import com.aerorescue.drone.domain.port.out.DroneRepository;
import com.aerorescue.drone.infrastructure.adapter.out.persistence.entity.DroneEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;

import java.util.*;

interface DroneJpaRepository extends JpaRepository<DroneEntity, UUID> {
    List<DroneEntity> findByStatus(DroneStatus status);

    @Query("SELECT d FROM DroneEntity d WHERE d.status IN ('AVAILABLE','ON_MISSION','RETURNING')")
    List<DroneEntity> findMonitorable();
}

@Component
@RequiredArgsConstructor
class DroneRepositoryAdapter implements DroneRepository {

    private final DroneJpaRepository jpa;

    @Override
    public Drone save(Drone d) { return toDomain(jpa.save(toEntity(d))); }

    @Override
    public Optional<Drone> findById(UUID id) { return jpa.findById(id).map(this::toDomain); }

    @Override
    public Optional<Drone> findByStringId(String id) {
        try { return findById(UUID.fromString(id)); } catch (Exception e) { return Optional.empty(); }
    }

    @Override
    public List<Drone> findAll() { return jpa.findAll().stream().map(this::toDomain).toList(); }

    @Override
    public List<Drone> findByStatus(DroneStatus status) {
        return jpa.findByStatus(status).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Drone> findMonitorable() {
        return jpa.findMonitorable().stream().map(this::toDomain).toList();
    }

    private Drone toDomain(DroneEntity e) {
        return Drone.builder().id(e.getId()).name(e.getName()).status(e.getStatus()).type(e.getType())
            .lat(e.getLat()).lng(e.getLng()).altitudeMeters(e.getAltitudeMeters())
            .batteryPercentage(e.getBatteryPercentage()).batteryStatus(e.getBatteryStatus())
            .missionId(e.getMissionId()).lastTelemetryAt(e.getLastTelemetryAt())
            .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt()).build();
    }

    private DroneEntity toEntity(Drone d) {
        return DroneEntity.builder().id(d.getId()).name(d.getName()).status(d.getStatus()).type(d.getType())
            .lat(d.getLat()).lng(d.getLng()).altitudeMeters(d.getAltitudeMeters())
            .batteryPercentage(d.getBatteryPercentage()).batteryStatus(d.getBatteryStatus())
            .missionId(d.getMissionId()).lastTelemetryAt(d.getLastTelemetryAt())
            .createdAt(d.getCreatedAt()).updatedAt(d.getUpdatedAt()).build();
    }
}
