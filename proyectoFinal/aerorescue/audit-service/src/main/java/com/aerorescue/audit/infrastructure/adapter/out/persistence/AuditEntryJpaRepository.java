package com.aerorescue.audit.infrastructure.adapter.out.persistence;

import com.aerorescue.audit.infrastructure.adapter.out.persistence.entity.AuditEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AuditEntryJpaRepository extends JpaRepository<AuditEntryEntity, UUID> {
    List<AuditEntryEntity> findByCorrelationIdOrderByOccurredAtAsc(String correlationId);
    List<AuditEntryEntity> findByEventTypeOrderByOccurredAtDesc(String eventType);
}
