package com.aerorescue.mission.infrastructure.adapter.in.kafka;

import com.aerorescue.mission.domain.model.*;
import com.aerorescue.mission.domain.port.in.ReassignMissionUseCase;
import com.aerorescue.mission.domain.port.out.DroneProjectionRepository;
import com.aerorescue.mission.domain.port.out.MissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DroneEventConsumer {

    private final DroneProjectionRepository projectionRepository;
    private final MissionRepository missionRepository;
    private final ReassignMissionUseCase reassignMissionUseCase;

    /**
     * Actualiza la proyección CQRS local con cada telemetría recibida.
     * Mission Service no llama a Drone Service — usa sus propios datos.
     */
    @KafkaListener(topics = "drone.telemetry", groupId = "mission-service-group")
    public void onTelemetry(Map<String, Object> event) {

        try {

            Map<String, Object> payload = (Map<String, Object>) event.get("payload");
            Map<String, Object> location = (Map<String, Object>) payload.get("location");
            Map<String, Object> battery = (Map<String, Object>) payload.get("battery");

            String droneId = (String) payload.get("droneId");
            String type = (String) payload.get("type");

            // FALTABA ESTO
            String status = (String) payload.get("status");

            projectionRepository.findByDroneId(droneId)
                    .ifPresentOrElse(existing -> {

                        DroneProjection updated = existing.toBuilder()
                                .status(status) // <-- IMPORTANTE
                                .type(type)
                                .lat(((Number) location.get("lat")).doubleValue())
                                .lng(((Number) location.get("lng")).doubleValue())
                                .batteryPercentage(((Number) battery.get("percentage")).intValue())
                                .lastUpdatedAt(Instant.now())
                                .build();

                        projectionRepository.save(updated);

                    }, () -> {

                        DroneProjection created = DroneProjection.builder()
                                .droneId(droneId)
                                .status(status) // <-- IMPORTANTE
                                .type(type)
                                .lat(((Number) location.get("lat")).doubleValue())
                                .lng(((Number) location.get("lng")).doubleValue())
                                .batteryPercentage(((Number) battery.get("percentage")).intValue())
                                .lastUpdatedAt(Instant.now())
                                .build();

                        projectionRepository.save(created);
                    });

        } catch (Exception e) {
            log.error("Error processing drone.telemetry for CQRS", e);
        }
    }

    @KafkaListener(topics = "drone.status.changed", groupId = "mission-service-group")
    public void onStatusChanged(Map<String, Object> event) {
        try {
            Map<String, Object> payload = (Map<String, Object>) event.get("payload");
            String droneId = (String) payload.get("droneId");
            String newStatus = (String) payload.get("newStatus");
            String missionId = (String) payload.get("missionId");

            projectionRepository.findByDroneId(droneId).ifPresentOrElse(existing -> {
                projectionRepository.save(existing.toBuilder()
                    .status(newStatus)
                    .missionId(missionId)
                    .lastUpdatedAt(Instant.now())
                    .build());
            }, () -> {
                // Crear proyección si no existe aún
                projectionRepository.save(DroneProjection.builder()
                    .droneId(droneId)
                    .status(newStatus)
                    .missionId(missionId)
                    .lastUpdatedAt(Instant.now())
                    .build());
            });
        } catch (Exception e) {
            log.error("Error processing drone.status.changed: {}", e.getMessage());
        }
    }

    /**
     * Battery LOW → misión pasa a AWAITING_REPLACEMENT → inicia graceful handoff
     * Battery CRITICAL → reasignación inmediata
     */
    @KafkaListener(topics = "drone.battery.low", groupId = "mission-service-group")
    public void onBatteryLow(Map<String, Object> event) {
        try {
            Map<String, Object> payload = (Map<String, Object>) event.get("payload");
            String droneId = (String) payload.get("droneId");
            String missionId = (String) payload.get("missionId");
            String severity = (String) payload.get("severity");

            if (missionId == null || missionId.isBlank()) return;

            missionRepository.findById(java.util.UUID.fromString(missionId)).ifPresent(mission -> {
                if (mission.getStatus() == MissionStatus.IN_PROGRESS
                        || mission.getStatus() == MissionStatus.ASSIGNED) {

                    if ("CRITICAL".equals(severity)) {
                        log.warn("Battery CRITICAL on drone {} — immediate reassignment", droneId);
                        reassignMissionUseCase.reassign(mission.getId(), "BATTERY_CRITICAL");
                    } else {
                        // WARNING: marcar AWAITING_REPLACEMENT e iniciar búsqueda
                        log.info("Battery WARNING on drone {} — graceful handoff initiated", droneId);
                        Mission awaiting = mission.toBuilder()
                            .status(MissionStatus.AWAITING_REPLACEMENT)
                            .updatedAt(Instant.now())
                            .build();
                        missionRepository.save(awaiting);
                        reassignMissionUseCase.reassign(mission.getId(), "BATTERY_LOW");
                    }
                }
            });
        } catch (Exception e) {
            log.error("Error processing drone.battery.low: {}", e.getMessage());
        }
    }

    /**
     * Drone OFFLINE → reasignación de emergencia inmediata
     */
    @KafkaListener(topics = "drone.offline", groupId = "mission-service-group")
    public void onDroneOffline(Map<String, Object> event) {
        try {
            Map<String, Object> payload = (Map<String, Object>) event.get("payload");
            String missionId = (String) payload.get("missionId");
            String droneId = (String) payload.get("droneId");

            if (missionId == null || missionId.isBlank()) return;

            missionRepository.findById(java.util.UUID.fromString(missionId)).ifPresent(mission -> {
                if (mission.getStatus() == MissionStatus.IN_PROGRESS
                        || mission.getStatus() == MissionStatus.ASSIGNED
                        || mission.getStatus() == MissionStatus.AWAITING_REPLACEMENT) {
                    log.warn("Drone {} OFFLINE — emergency reassignment for mission {}", droneId, missionId);
                    reassignMissionUseCase.reassign(mission.getId(), "DRONE_OFFLINE");
                }
            });
        } catch (Exception e) {
            log.error("Error processing drone.offline: {}", e.getMessage());
        }
    }
}
