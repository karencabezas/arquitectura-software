package com.aerorescue.mission.infrastructure.adapter.out.persistence;

import com.aerorescue.mission.domain.model.*;
import com.aerorescue.mission.domain.port.out.*;
import com.aerorescue.mission.infrastructure.adapter.out.persistence.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;

import java.util.*;

// ── JPA Spring Data interfaces ─────────────────────────
interface MissionJpaRepository extends JpaRepository<MissionEntity, UUID> {
    List<MissionEntity> findByStatus(MissionStatus status);

    @Query("SELECT m FROM MissionEntity m WHERE m.emergencyId = :emergencyId " +
           "AND m.status NOT IN ('COMPLETED','FAILED')")
    Optional<MissionEntity> findActiveByEmergencyId(String emergencyId);
}

interface DroneProjectionJpaRepository extends JpaRepository<DroneProjectionEntity, String> {
    @Query("SELECT d FROM DroneProjectionEntity d WHERE d.status = 'AVAILABLE' AND d.batteryPercentage >= 20")
    List<DroneProjectionEntity> findAvailable();
}

// ── MissionRepository Adapter ──────────────────────────
@Component
@RequiredArgsConstructor
class MissionRepositoryAdapter implements MissionRepository {

    private final MissionJpaRepository jpa;

    @Override
    public Mission save(Mission m) { return toDomain(jpa.save(toEntity(m))); }

    @Override
    public Optional<Mission> findById(UUID id) { return jpa.findById(id).map(this::toDomain); }

    @Override
    public Optional<Mission> findActiveByEmergencyId(String emergencyId) {
        return jpa.findActiveByEmergencyId(emergencyId).map(this::toDomain);
    }

    @Override
    public List<Mission> findByStatus(MissionStatus status) {
        return jpa.findByStatus(status).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Mission> findAll() { return jpa.findAll().stream().map(this::toDomain).toList(); }

    private Mission toDomain(MissionEntity e) {
        return Mission.builder().id(e.getId()).emergencyId(e.getEmergencyId())
            .droneId(e.getDroneId()).status(e.getStatus()).outcome(e.getOutcome())
            .missionType(e.getMissionType()).priority(e.getPriority())
            .assignedAt(e.getAssignedAt()).unattendedSince(e.getUnattendedSince())
            .completedAt(e.getCompletedAt()).closedBy(e.getClosedBy())
            .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt()).build();
    }

    private MissionEntity toEntity(Mission m) {
        return MissionEntity.builder().id(m.getId()).emergencyId(m.getEmergencyId())
            .droneId(m.getDroneId()).status(m.getStatus()).outcome(m.getOutcome())
            .missionType(m.getMissionType()).priority(m.getPriority())
            .assignedAt(m.getAssignedAt()).unattendedSince(m.getUnattendedSince())
            .completedAt(m.getCompletedAt()).closedBy(m.getClosedBy())
            .createdAt(m.getCreatedAt()).updatedAt(m.getUpdatedAt()).build();
    }
}

// ── DroneProjectionRepository Adapter ─────────────────
@Component
@RequiredArgsConstructor
class DroneProjectionRepositoryAdapter implements DroneProjectionRepository {

    private final DroneProjectionJpaRepository jpa;

    @Override
    public void save(DroneProjection p) { jpa.save(toEntity(p)); }

    @Override
    public Optional<DroneProjection> findByDroneId(String droneId) {
        return jpa.findById(droneId).map(this::toDomain);
    }

    @Override
    public List<DroneProjection> findAvailable() {
        return jpa.findAvailable().stream().map(this::toDomain).toList();
    }

    @Override
    public List<DroneProjection> findAll() {
        return jpa.findAll().stream().map(this::toDomain).toList();
    }

    private DroneProjection toDomain(DroneProjectionEntity e) {
        return DroneProjection.builder().droneId(e.getDroneId()).status(e.getStatus())
            .type(e.getType()).lat(e.getLat()).lng(e.getLng())
            .batteryPercentage(e.getBatteryPercentage()).missionId(e.getMissionId())
            .lastUpdatedAt(e.getLastUpdatedAt()).build();
    }

    private DroneProjectionEntity toEntity(DroneProjection p) {
        return DroneProjectionEntity.builder().droneId(p.getDroneId()).status(p.getStatus())
            .type(p.getType()).lat(p.getLat()).lng(p.getLng())
            .batteryPercentage(p.getBatteryPercentage()).missionId(p.getMissionId())
            .lastUpdatedAt(p.getLastUpdatedAt()).build();
    }
}
