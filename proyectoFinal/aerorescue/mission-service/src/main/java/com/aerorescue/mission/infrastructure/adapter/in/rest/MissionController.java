package com.aerorescue.mission.infrastructure.adapter.in.rest;

import com.aerorescue.mission.domain.model.*;
import com.aerorescue.mission.domain.port.in.*;
import com.aerorescue.mission.domain.port.out.MissionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/missions")
@RequiredArgsConstructor
@Tag(name = "Missions", description = "Coordinación y gestión de misiones")
public class MissionController {

    private final MissionRepository missionRepository;
    private final ReassignMissionUseCase reassignUseCase;
    private final CloseMissionUseCase closeUseCase;

    @GetMapping
    @Operation(summary = "Listar todas las misiones")
    public ResponseEntity<List<MissionResponse>> getAll() {
        return ResponseEntity.ok(
            missionRepository.findAll().stream().map(MissionResponse::from).toList()
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener misión por ID")
    public ResponseEntity<MissionResponse> getById(@PathVariable UUID id) {
        return missionRepository.findById(id)
            .map(m -> ResponseEntity.ok(MissionResponse.from(m)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/reassign")
    @Operation(summary = "Reasignar misión manualmente — requiere clearance_level >= 3")
    public ResponseEntity<MissionResponse> reassign(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "MANUAL") String reason,
            HttpServletRequest http) {
        int clearance = http.getAttribute("clearanceLevel") != null
            ? (int) http.getAttribute("clearanceLevel") : 0;
        if (clearance < 3) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(MissionResponse.from(reassignUseCase.reassign(id, reason)));
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "Cerrar misión manualmente con outcome")
    public ResponseEntity<MissionResponse> close(
            @PathVariable UUID id,
            @RequestParam MissionOutcome outcome,
            HttpServletRequest http) {
        String userId = (String) http.getAttribute("userId");
        int clearance = http.getAttribute("clearanceLevel") != null
            ? (int) http.getAttribute("clearanceLevel") : 0;
        return ResponseEntity.ok(MissionResponse.from(closeUseCase.close(id, outcome, userId, clearance)));
    }

    public record MissionResponse(UUID id, String emergencyId, String droneId,
                                   MissionStatus status, MissionOutcome outcome,
                                   String missionType, String priority) {
        public static MissionResponse from(Mission m) {
            return new MissionResponse(m.getId(), m.getEmergencyId(), m.getDroneId(),
                m.getStatus(), m.getOutcome(), m.getMissionType(), m.getPriority());
        }
    }
}
