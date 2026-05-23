package com.aerorescue.drone.application.usecase;

import com.aerorescue.drone.domain.model.*;
import com.aerorescue.drone.domain.port.in.UpdateDroneStatusUseCase;
import com.aerorescue.drone.domain.port.out.DroneEventPublisher;
import com.aerorescue.drone.domain.port.out.DroneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateDroneStatusUseCaseImpl implements UpdateDroneStatusUseCase {

    private final DroneRepository repository;
    private final DroneEventPublisher eventPublisher;

    @Override
    public Drone updateStatus(UUID droneId, DroneStatus newStatus, String missionId) {
        Drone drone = repository.findById(droneId)
            .orElseThrow(() -> new RuntimeException("Drone not found: " + droneId));

        DroneStatus previousStatus = drone.getStatus();
        Drone updated = drone.toBuilder()
            .status(newStatus)
            .missionId(missionId)
            .updatedAt(Instant.now())
            .build();

        Drone saved = repository.save(updated);
        eventPublisher.publishStatusChanged(saved, previousStatus);
        return saved;
    }
}
