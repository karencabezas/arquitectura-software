package com.aerorescue.emergency.infrastructure.adapter.in.rest;

import com.aerorescue.emergency.domain.model.*;
import com.aerorescue.emergency.domain.port.in.*;
import com.aerorescue.emergency.infrastructure.adapter.in.rest.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/emergencies")
@RequiredArgsConstructor
@Tag(name = "Emergencies", description = "Gestión de emergencias")
public class EmergencyController {

    private final CreateEmergencyUseCase createUseCase;
    private final GetEmergencyUseCase getUseCase;
    private final UpdateEmergencyStatusUseCase updateStatusUseCase;
    private final EscalateEmergencyUseCase escalateUseCase;

    @PostMapping
    @Operation(summary = "Crear nueva emergencia")
    public ResponseEntity<EmergencyResponse> create(
            @Valid @RequestBody CreateEmergencyRequest req,
            HttpServletRequest http) {

        Emergency emergency = createUseCase.create(new CreateEmergencyUseCase.Command(
            req.priority(), req.type(),
            Location.builder()
                .address(req.address()).region(req.region())
                .lat(req.lat()).lng(req.lng()).build(),
            req.description(),
            (String) http.getAttribute("userId"),
            (String) http.getAttribute("organizationId"),
            (String) http.getAttribute("role"),
            (String) http.getAttribute("region"),
            (int) http.getAttribute("clearanceLevel")
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(EmergencyResponse.from(emergency));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener emergencia por ID")
    public ResponseEntity<EmergencyResponse> getById(
            @PathVariable UUID id, HttpServletRequest http) {
        Emergency e = getUseCase.getById(id,
            (String) http.getAttribute("role"),
            (String) http.getAttribute("region"),
            (String) http.getAttribute("organizationId"));
        return ResponseEntity.ok(EmergencyResponse.from(e));
    }

    @GetMapping
    @Operation(summary = "Listar emergencias según permisos del usuario")
    public ResponseEntity<List<EmergencyResponse>> getAll(HttpServletRequest http) {
        List<EmergencyResponse> list = getUseCase.getAll(
            (String) http.getAttribute("role"),
            (String) http.getAttribute("region"),
            (String) http.getAttribute("organizationId")
        ).stream().map(EmergencyResponse::from).toList();
        return ResponseEntity.ok(list);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Actualizar estado de emergencia")
    public ResponseEntity<EmergencyResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStatusRequest req,
            HttpServletRequest http) {
        Emergency e = updateStatusUseCase.updateStatus(id, req.status(),
            (String) http.getAttribute("userId"),
            (String) http.getAttribute("role"),
            (String) http.getAttribute("organizationId"));
        return ResponseEntity.ok(EmergencyResponse.from(e));
    }

    @PostMapping("/{id}/escalate")
    @Operation(summary = "Escalar prioridad de emergencia — requiere clearance_level >= 3")
    public ResponseEntity<EmergencyResponse> escalate(
            @PathVariable UUID id, HttpServletRequest http) {
        Emergency e = escalateUseCase.escalate(id,
            (String) http.getAttribute("userId"),
            (int) http.getAttribute("clearanceLevel"));
        return ResponseEntity.ok(EmergencyResponse.from(e));
    }
}
