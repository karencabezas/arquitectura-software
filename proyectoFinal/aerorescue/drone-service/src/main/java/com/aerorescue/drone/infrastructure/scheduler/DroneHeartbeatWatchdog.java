package com.aerorescue.drone.infrastructure.scheduler;

import com.aerorescue.drone.domain.model.*;
import com.aerorescue.drone.domain.port.out.DroneEventPublisher;
import com.aerorescue.drone.domain.port.out.DroneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Watchdog que detecta drones offline.
 * Umbrales acordados:
 *   WARNING: 15 segundos sin telemetría (alerta interna)
 *   OFFLINE:  30 segundos sin telemetría (publica drone.offline)
 *
 * Solo monitorea drones en AVAILABLE, ON_MISSION, RETURNING.
 * Ignora INACTIVE y MAINTENANCE.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DroneHeartbeatWatchdog {

    private final DroneRepository droneRepository;
    private final DroneEventPublisher eventPublisher;

    @Value("${drone.heartbeat.warning-threshold-seconds:15}")
    private int warningThresholdSeconds;

    @Value("${drone.heartbeat.offline-threshold-seconds:300}")
    private int offlineThresholdSeconds;

    @Scheduled(fixedRateString = "${drone.heartbeat.check-interval-ms:5000}")
    public void checkHeartbeats() {
        List<Drone> monitorable = droneRepository.findMonitorable();
        Instant now = Instant.now();

        for (Drone drone : monitorable) {
            if (drone.getLastTelemetryAt() == null) continue;

            long secondsSinceLast = Duration.between(drone.getLastTelemetryAt(), now).getSeconds();

            if (secondsSinceLast >= offlineThresholdSeconds
                    && drone.getStatus() != DroneStatus.OFFLINE) {
                log.warn("Drone {} OFFLINE — {}s without telemetry", drone.getId(), secondsSinceLast);
                Drone offline = drone.toBuilder()
                    .status(DroneStatus.OFFLINE)
                    .updatedAt(now)
                    .build();
                droneRepository.save(offline);
                eventPublisher.publishOffline(offline);

            } else if (secondsSinceLast >= warningThresholdSeconds) {
                log.info("Drone {} WARNING — {}s without telemetry (watching...)",
                    drone.getId(), secondsSinceLast);
                // Solo alerta interna — no publica evento externo
            }
        }
    }
}
