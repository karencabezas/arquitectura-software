package com.aerorescue.audit.infrastructure.adapter.in.rest;

import com.aerorescue.audit.infrastructure.adapter.out.persistence.AuditEntryJpaRepository;
import com.aerorescue.audit.infrastructure.adapter.out.persistence.entity.AuditEntryEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "Trazabilidad de eventos por correlationId")
public class AuditController {

    private final AuditEntryJpaRepository repository;

    @GetMapping("/correlation/{correlationId}")
    @Operation(summary = "Reconstruir el historial completo de una operación por correlationId")
    public ResponseEntity<List<AuditEntryEntity>> getByCorrelation(@PathVariable String correlationId) {
        return ResponseEntity.ok(
            repository.findByCorrelationIdOrderByOccurredAtAsc(correlationId)
        );
    }

    @GetMapping("/events/{eventType}")
    @Operation(summary = "Listar eventos por tipo")
    public ResponseEntity<List<AuditEntryEntity>> getByEventType(@PathVariable String eventType) {
        return ResponseEntity.ok(
            repository.findByEventTypeOrderByOccurredAtDesc(eventType)
        );
    }
}
