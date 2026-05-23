package com.aerorescue.drone.infrastructure.adapter.in.kafka;

import com.aerorescue.drone.domain.model.DroneStatus;
import com.aerorescue.drone.domain.port.in.UpdateDroneStatusUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MissionEventConsumer {

    private final UpdateDroneStatusUseCase updateStatusUseCase;

    @KafkaListener(topics = "mission.assigned", groupId = "drone-service-group")
    public void onMissionAssigned(Map<String, Object> event) {
        try {
            Map<String, Object> payload = (Map<String, Object>) event.get("payload");
            String droneId = (String) payload.get("droneId");
            String missionId = (String) payload.get("missionId");
            log.info("Mission assigned — updating drone {} to ON_MISSION", droneId);
            updateStatusUseCase.updateStatus(UUID.fromString(droneId), DroneStatus.ON_MISSION, missionId);
        } catch (Exception e) {
            log.error("Error processing mission.assigned: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "mission.completed", groupId = "drone-service-group")
    public void onMissionCompleted(Map<String, Object> event) {
        try {
            Map<String, Object> payload = (Map<String, Object>) event.get("payload");
            String droneId = (String) payload.get("droneId");
            log.info("Mission completed — updating drone {} to RETURNING", droneId);
            updateStatusUseCase.updateStatus(UUID.fromString(droneId), DroneStatus.RETURNING, null);
        } catch (Exception e) {
            log.error("Error processing mission.completed: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "mission.reassigned", groupId = "drone-service-group")
    public void onMissionReassigned(Map<String, Object> event) {
        try {
            Map<String, Object> payload = (Map<String, Object>) event.get("payload");
            String previousDroneId = (String) payload.get("previousDroneId");
            String newDroneId = (String) payload.get("newDroneId");
            String missionId = (String) payload.get("missionId");
            // Drone anterior → RETURNING (ya no tiene misión)
            updateStatusUseCase.updateStatus(UUID.fromString(previousDroneId), DroneStatus.RETURNING, null);
            // Nuevo drone → ON_MISSION
            updateStatusUseCase.updateStatus(UUID.fromString(newDroneId), DroneStatus.ON_MISSION, missionId);
            log.info("Reassignment processed — {} → RETURNING, {} → ON_MISSION",
                previousDroneId, newDroneId);
        } catch (Exception e) {
            log.error("Error processing mission.reassigned: {}", e.getMessage());
        }
    }
}
