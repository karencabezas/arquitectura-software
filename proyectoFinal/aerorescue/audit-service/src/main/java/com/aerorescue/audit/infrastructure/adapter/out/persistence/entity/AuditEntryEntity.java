package com.aerorescue.audit.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_entries")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditEntryEntity {
    @Id private UUID id;
    @Column(name = "event_id")      private String eventId;
    @Column(name = "event_type")    private String eventType;
    @Column(name = "correlation_id") private String correlationId;
    private String source;
    @Column(columnDefinition = "TEXT") private String payload;
    @Column(name = "occurred_at")  private Instant occurredAt;
    @Column(name = "recorded_at")  private Instant recordedAt;
}
