package com.aerorescue.emergency.infrastructure.adapter.in.kafka;

import com.aerorescue.emergency.domain.model.EmergencyStatus;
import com.aerorescue.emergency.domain.port.in.UpdateEmergencyStatusUseCase;
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

    private final UpdateEmergencyStatusUseCase updateStatusUseCase;

    @KafkaListener(topics = "mission.completed", groupId = "emergency-service-group")
    public void onMissionCompleted(Map<String, Object> event) {
        try {
            Map<String, Object> payload = (Map<String, Object>) event.get("payload");
            String emergencyId = (String) payload.get("emergencyId");
            String outcome     = (String) payload.get("outcome");

            if (emergencyId == null || emergencyId.isBlank()) return;

            // Map mission outcome to emergency status
            EmergencyStatus newStatus = switch (outcome != null ? outcome : "SUCCESS") {
                case "SUCCESS"                -> EmergencyStatus.RESOLVED;
                case "RESOLVED_WITHOUT_DRONE" -> EmergencyStatus.RESOLVED;
                case "TIMEOUT"               -> EmergencyStatus.CANCELLED;
                case "DRONE_FAILURE"         -> EmergencyStatus.CANCELLED;
                default                      -> EmergencyStatus.RESOLVED;
            };

            updateStatusUseCase.updateStatus(
                UUID.fromString(emergencyId),
                newStatus,
                "SYSTEM",
                "SYSTEM",
                "ORG-001"
            );

            log.info("Emergency {} updated to {} after mission completed with outcome {}",
                emergencyId, newStatus, outcome);

        } catch (Exception e) {
            log.error("Error processing mission.completed in emergency service: {}", e.getMessage());
        }
    }
}
