package com.aerorescue.emergency.infrastructure.adapter.out.kafka;

import com.aerorescue.emergency.domain.model.Emergency;
import com.aerorescue.emergency.domain.model.EmergencyPriority;
import com.aerorescue.emergency.domain.model.EmergencyStatus;
import com.aerorescue.emergency.domain.port.out.EmergencyEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmergencyEventPublisherImpl implements EmergencyEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishCreated(Emergency e) {
        Map<String, Object> event = buildEnvelope("emergency.created", "emergency-service", Map.of(
            "emergencyId", e.getId().toString(),
            "priority", e.getPriority().name(),
            "type", e.getType().name(),
            "location", Map.of(
                "address", e.getLocation().getAddress(),
                "region", e.getLocation().getRegion(),
                "lat", e.getLocation().getLat(),
                "lng", e.getLocation().getLng()
            ),
            "reportedBy", e.getReportedBy(),
            "organizationId", e.getOrganizationId(),
            "description", e.getDescription() != null ? e.getDescription() : ""
        ));
        send("emergency.created", e.getId().toString(), event);
    }

    @Override
    public void publishUpdated(Emergency e, EmergencyStatus previousStatus, String updatedBy) {
        Map<String, Object> event = buildEnvelope("emergency.updated", "emergency-service", Map.of(
            "emergencyId", e.getId().toString(),
            "previousStatus", previousStatus.name(),
            "newStatus", e.getStatus().name(),
            "updatedBy", updatedBy
        ));
        send("emergency.updated", e.getId().toString(), event);
    }

    @Override
    public void publishEscalated(Emergency e, EmergencyPriority previousPriority, String escalatedBy) {
        Map<String, Object> event = buildEnvelope("emergency.escalated", "emergency-service", Map.of(
            "emergencyId", e.getId().toString(),
            "previousPriority", previousPriority.name(),
            "newPriority", e.getPriority().name(),
            "escalatedBy", escalatedBy
        ));
        send("emergency.escalated", e.getId().toString(), event);
    }

    private Map<String, Object> buildEnvelope(String eventType, String source, Map<String, Object> payload) {
        return Map.of(
            "eventId", UUID.randomUUID().toString(),
            "eventType", eventType,
            "eventVersion", "1.0",
            "occurredAt", Instant.now().toString(),
            "source", source,
            "correlationId", UUID.randomUUID().toString(),
            "payload", payload
        );
    }

    private void send(String topic, String key, Object payload) {
        kafkaTemplate.send(topic, key, payload)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish event to topic {}: {}", topic, ex.getMessage());
                } else {
                    log.debug("Event published to topic {}, key={}", topic, key);
                }
            });
    }
}
