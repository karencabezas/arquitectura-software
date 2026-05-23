package com.aerorescue.audit.infrastructure.adapter.in.kafka;

import com.aerorescue.audit.infrastructure.adapter.out.persistence.AuditEntryJpaRepository;
import com.aerorescue.audit.infrastructure.adapter.out.persistence.entity.AuditEntryEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Audit Service consume TODOS los topics y persiste cada evento
 * con su correlationId para trazabilidad completa.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventConsumer {

    private final AuditEntryJpaRepository repository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = {
        "emergency.created", "emergency.updated", "emergency.escalated",
        "drone.telemetry", "drone.battery.low", "drone.offline", "drone.status.changed",
        "mission.assigned", "mission.reassigned", "mission.completed",
        "alert.created"
    }, groupId = "audit-service-group")
    public void onAnyEvent(Map<String, Object> event) {
        try {
            String eventType    = (String) event.get("eventType");
            String eventId      = (String) event.get("eventId");
            String correlationId = (String) event.get("correlationId");
            String occurredAt   = (String) event.get("occurredAt");
            String source       = (String) event.get("source");
            String payload      = objectMapper.writeValueAsString(event.get("payload"));

            AuditEntryEntity entry = AuditEntryEntity.builder()
                .id(UUID.randomUUID())
                .eventId(eventId)
                .eventType(eventType)
                .correlationId(correlationId)
                .source(source)
                .payload(payload)
                .occurredAt(occurredAt != null ? Instant.parse(occurredAt) : Instant.now())
                .recordedAt(Instant.now())
                .build();

            repository.save(entry);
            log.debug("Audit recorded: {} [correlationId={}]", eventType, correlationId);

        } catch (Exception e) {
            log.error("Error recording audit event: {}", e.getMessage());
        }
    }
}
