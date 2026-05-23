package com.aerorescue.drone.application.usecase;

import com.aerorescue.drone.domain.model.*;
import com.aerorescue.drone.domain.port.in.RegisterDroneUseCase;
import com.aerorescue.drone.domain.port.out.DroneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterDroneUseCaseImpl implements RegisterDroneUseCase {

    private final DroneRepository repository;

    @Override
    public Drone register(String name, DroneType type, double lat, double lng) {
        Drone drone = Drone.builder()
            .id(UUID.randomUUID())
            .name(name)
            .status(DroneStatus.INACTIVE)
            .type(type)
            .lat(lat).lng(lng)
            .batteryPercentage(100)
            .batteryStatus(BatteryStatus.NORMAL)
            .lastTelemetryAt(Instant.now())
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
        return repository.save(drone);
    }
}
