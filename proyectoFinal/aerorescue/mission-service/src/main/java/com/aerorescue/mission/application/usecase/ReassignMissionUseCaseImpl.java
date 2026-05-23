package com.aerorescue.mission.application.usecase;

import com.aerorescue.mission.domain.model.*;
import com.aerorescue.mission.domain.port.in.ReassignMissionUseCase;
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
public class ReassignMissionUseCaseImpl implements ReassignMissionUseCase {

    private final MissionRepository missionRepository;
    private final DroneProjectionRepository droneProjectionRepository;
    private final MissionEventPublisher eventPublisher;
    private final DroneSelectionService selectionService;

    @Override
    public Mission reassign(UUID missionId, String reason) {
        Mission mission = missionRepository.findById(missionId)
            .orElseThrow(() -> new RuntimeException("Mission not found: " + missionId));

        // Buscar drone de reemplazo excluyendo el actual
        List<DroneProjection> candidates = droneProjectionRepository.findAvailable()
            .stream()
            .filter(d -> !d.getDroneId().equals(mission.getDroneId()))
            .toList();

        // Obtener ubicación de la emergencia desde proyección actual del drone
        DroneProjection currentDrone = droneProjectionRepository
            .findByDroneId(mission.getDroneId()).orElse(null);

        double lat = currentDrone != null ? currentDrone.getLat() : 0;
        double lng = currentDrone != null ? currentDrone.getLng() : 0;

        return selectionService.selectBest(candidates, mission.getMissionType(), lat, lng)
            .map(newDrone -> {
                String previousDroneId = mission.getDroneId();

                // Graceful handoff: transición AWAITING_REPLACEMENT → REASSIGNED → IN_PROGRESS
                Mission reassigned = mission.toBuilder()
                    .droneId(newDrone.getDroneId())
                    .status(MissionStatus.IN_PROGRESS)
                    .updatedAt(Instant.now())
                    .build();

                Mission saved = missionRepository.save(reassigned);
                eventPublisher.publishReassigned(saved, previousDroneId, reason);
                log.info("Mission {} reassigned from {} to {} — reason: {}",
                    missionId, previousDroneId, newDrone.getDroneId(), reason);
                return saved;

            }).orElseGet(() -> {
                // Sin reemplazo → UNATTENDED
                log.warn("No replacement drone for mission {} — going UNATTENDED", missionId);
                Mission unattended = mission.toBuilder()
                    .status(MissionStatus.UNATTENDED)
                    .unattendedSince(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
                return missionRepository.save(unattended);
            });
    }
}
