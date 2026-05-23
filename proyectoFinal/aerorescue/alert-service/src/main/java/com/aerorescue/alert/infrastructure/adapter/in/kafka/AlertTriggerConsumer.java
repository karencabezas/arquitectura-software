package com.aerorescue.alert.infrastructure.adapter.in.kafka;

import com.aerorescue.alert.domain.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertTriggerConsumer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "drone.battery.low", groupId = "alert-service-group")
    public void onBatteryLow(Map<String, Object> event) {
        Map<String, Object> payload = (Map<String, Object>) event.get("payload");
        String severity = (String) payload.get("severity");
        int battery = ((Number) payload.get("batteryPercentage")).intValue();

        Alert alert = Alert.builder()
            .id(UUID.randomUUID())
            .type(AlertType.BATTERY_LOW)
            .severity("CRITICAL".equals(severity) ? AlertSeverity.CRITICAL : AlertSeverity.HIGH)
            .relatedEntityType("DRONE")
            .relatedEntityId((String) payload.get("droneId"))
            .missionId((String) payload.get("missionId"))
            .message(String.format("Drone %s battery at %d%% — severity: %s",
                payload.get("droneId"), battery, severity))
            .autoResolvable(false)
            .createdAt(Instant.now())
            .build();

        publishAlert(alert);
    }

    @KafkaListener(topics = "drone.offline", groupId = "alert-service-group")
    public void onDroneOffline(Map<String, Object> event) {
        Map<String, Object> payload = (Map<String, Object>) event.get("payload");

        Alert alert = Alert.builder()
            .id(UUID.randomUUID())
            .type(AlertType.DRONE_OFFLINE)
            .severity(AlertSeverity.CRITICAL)
            .relatedEntityType("DRONE")
            .relatedEntityId((String) payload.get("droneId"))
            .missionId((String) payload.get("missionId"))
            .message(String.format("Drone %s went OFFLINE during active mission", payload.get("droneId")))
            .autoResolvable(true)
            .createdAt(Instant.now())
            .build();

        publishAlert(alert);
    }

    @KafkaListener(topics = "emergency.escalated", groupId = "alert-service-group")
    public void onEmergencyEscalated(Map<String, Object> event) {
        Map<String, Object> payload = (Map<String, Object>) event.get("payload");

        Alert alert = Alert.builder()
            .id(UUID.randomUUID())
            .type(AlertType.EMERGENCY_ESCALATED)
            .severity(AlertSeverity.HIGH)
            .relatedEntityType("EMERGENCY")
            .relatedEntityId((String) payload.get("emergencyId"))
            .message(String.format("Emergency %s escalated from %s to %s",
                payload.get("emergencyId"), payload.get("previousPriority"), payload.get("newPriority")))
            .autoResolvable(false)
            .createdAt(Instant.now())
            .build();

        publishAlert(alert);
    }

    private void publishAlert(Alert alert) {
        Map<String, Object> event = Map.of(
            "eventId",       UUID.randomUUID().toString(),
            "eventType",     "alert.created",
            "eventVersion",  "1.0",
            "occurredAt",    Instant.now().toString(),
            "source",        "alert-service",
            "correlationId", UUID.randomUUID().toString(),
            "payload", Map.of(
                "alertId",           alert.getId().toString(),
                "type",              alert.getType().name(),
                "severity",          alert.getSeverity().name(),
                "relatedEntityType", alert.getRelatedEntityType(),
                "relatedEntityId",   alert.getRelatedEntityId() != null ? alert.getRelatedEntityId() : "",
                "missionId",         alert.getMissionId() != null ? alert.getMissionId() : "",
                "emergencyId",       alert.getEmergencyId() != null ? alert.getEmergencyId() : "",
                "message",           alert.getMessage(),
                "autoResolvable",    alert.isAutoResolvable(),
                "createdAt",         alert.getCreatedAt().toString()
            )
        );

        kafkaTemplate.send("alert.created", alert.getId().toString(), event)
            .whenComplete((r, ex) -> {
                if (ex != null) log.error("Failed to publish alert: {}", ex.getMessage());
                else log.info("Alert published: {} — {}", alert.getType(), alert.getMessage());
            });
    }
}
