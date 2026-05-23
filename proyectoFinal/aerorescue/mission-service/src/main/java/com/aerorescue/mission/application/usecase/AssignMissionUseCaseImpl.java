package com.aerorescue.mission.application.usecase;

import com.aerorescue.mission.domain.model.*;
import com.aerorescue.mission.domain.port.in.AssignMissionUseCase;
import com.aerorescue.mission.domain.port.out.*;
import com.aerorescue.mission.domain.service.DroneSelectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssignMissionUseCaseImpl implements AssignMissionUseCase {

    private final MissionRepository missionRepository;
    private final DroneProjectionRepository droneProjectionRepository;
    private final MissionEventPublisher eventPublisher;
    private final DroneSelectionService selectionService;

    @Override
    public Mission assign(Command cmd) {
        List<DroneProjection> candidates = droneProjectionRepository.findAvailable();

        return selectionService.selectBest(
            candidates, cmd.missionType(), cmd.emergencyLat(), cmd.emergencyLng()
        ).map(drone -> {
            Mission mission = Mission.builder()
                .id(UUID.randomUUID())
                .emergencyId(cmd.emergencyId())
                .droneId(drone.getDroneId())
                .status(MissionStatus.ASSIGNED)
                .missionType(cmd.missionType())
                .priority(cmd.priority())
                .assignedAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

            Mission saved = missionRepository.save(mission);
            eventPublisher.publishAssigned(saved);
            log.info("Mission {} assigned to drone {}", saved.getId(), drone.getDroneId());
            return saved;

        }).orElseGet(() -> {
            // Sin drone disponible → UNATTENDED desde el inicio
            log.warn("No drone available for emergency {} — creating UNATTENDED mission", cmd.emergencyId());
            Mission unattended = Mission.builder()
                .id(UUID.randomUUID())
                .emergencyId(cmd.emergencyId())
                .status(MissionStatus.UNATTENDED)
                .missionType(cmd.missionType())
                .priority(cmd.priority())
                .unattendedSince(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
            return missionRepository.save(unattended);
        });
    }
}
