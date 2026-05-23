package com.aerorescue.drone.application.usecase;

import com.aerorescue.drone.domain.model.*;
import com.aerorescue.drone.domain.port.in.ProcessTelemetryUseCase;
import com.aerorescue.drone.domain.port.out.DroneEventPublisher;
import com.aerorescue.drone.domain.port.out.DroneRepository;
import com.aerorescue.drone.domain.service.BatteryMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessTelemetryUseCaseImpl implements ProcessTelemetryUseCase {

    private final DroneRepository droneRepository;
    private final DroneEventPublisher eventPublisher;
    private final BatteryMonitorService batteryMonitor;

    @Override
    public Drone process(TelemetryData telemetry) {
        Drone drone = droneRepository.findByStringId(telemetry.droneId())
            .orElseThrow(() -> new RuntimeException("Drone not found: " + telemetry.droneId()));

        Drone updated = drone.applyTelemetry(telemetry);
        Drone saved = droneRepository.save(updated);

        // Publicar telemetría
        eventPublisher.publishTelemetry(saved, telemetry);

        // Evaluar batería — umbrales acordados WARNING<20% / CRITICAL<5%
        BatteryMonitorService.BatteryAlertResult result = batteryMonitor.evaluate(saved);
        if (result.shouldAlert()) {
            log.warn("Battery alert for drone {}: {}% - {}",
                saved.getId(), result.batteryPercentage(), result.severity());
            eventPublisher.publishBatteryLow(saved, result.severity());

            // CRITICAL: forzar RETURNING inmediatamente
            if (result.severity() == BatteryStatus.CRITICAL
                    && saved.getStatus() == DroneStatus.ON_MISSION) {
                log.warn("CRITICAL battery on drone {} — forcing RETURNING", saved.getId());
                Drone returning = saved.toBuilder()
                    .status(DroneStatus.RETURNING)
                    .build();
                Drone savedReturning = droneRepository.save(returning);
                eventPublisher.publishStatusChanged(savedReturning, DroneStatus.ON_MISSION);
                return savedReturning;
            }
        }

        return saved;
    }
}
