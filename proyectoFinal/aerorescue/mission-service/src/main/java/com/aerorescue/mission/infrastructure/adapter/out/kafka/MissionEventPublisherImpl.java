package com.aerorescue.mission.infrastructure.adapter.out.kafka;

import com.aerorescue.mission.domain.model.Mission;
import com.aerorescue.mission.domain.port.out.MissionEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MissionEventPublisherImpl implements MissionEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishAssigned(Mission m) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("missionId", m.getId().toString());
        payload.put("emergencyId", m.getEmergencyId());
        payload.put("droneId", m.getDroneId());
        payload.put("assignedAt", m.getAssignedAt() != null ? m.getAssignedAt().toString() : Instant.now().toString());
        payload.put("assignedBy", "SYSTEM");
        payload.put("missionType", m.getMissionType());
        payload.put("priority", m.getPriority());
        send("mission.assigned", m.getId().toString(), envelope("mission.assigned", payload));
    }

    @Override
    public void publishReassigned(Mission m, String previousDroneId, String reason) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("missionId", m.getId().toString());
        payload.put("emergencyId", m.getEmergencyId());
        payload.put("previousDroneId", previousDroneId);
        payload.put("newDroneId", m.getDroneId());
        payload.put("reason", reason);
        payload.put("reassignedAt", Instant.now().toString());
        payload.put("reassignedBy", "SYSTEM");
        send("mission.reassigned", m.getId().toString(), envelope("mission.reassigned", payload));
    }

    @Override
    public void publishCompleted(Mission m) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("missionId", m.getId().toString());
        payload.put("emergencyId", m.getEmergencyId());
        payload.put("droneId", m.getDroneId() != null ? m.getDroneId() : "");
        payload.put("completedAt", m.getCompletedAt() != null ? m.getCompletedAt().toString() : Instant.now().toString());
        payload.put("outcome", m.getOutcome() != null ? m.getOutcome().name() : "SUCCESS");
        payload.put("closedBy", m.getClosedBy() != null ? m.getClosedBy() : "SYSTEM");
        send("mission.completed", m.getId().toString(), envelope("mission.completed", payload));
    }

    private Map<String, Object> envelope(String eventType, Map<String, Object> payload) {
        return Map.of(
            "eventId",       UUID.randomUUID().toString(),
            "eventType",     eventType,
            "eventVersion",  "1.0",
            "occurredAt",    Instant.now().toString(),
            "source",        "mission-service",
            "correlationId", UUID.randomUUID().toString(),
            "payload",       payload
        );
    }

    private void send(String topic, String key, Object payload) {
        kafkaTemplate.send(topic, key, payload)
            .whenComplete((r, ex) -> {
                if (ex != null) log.error("Failed to publish to {}: {}", topic, ex.getMessage());
                else log.debug("Published to topic {}", topic);
            });
    }
}
