package com.aerorescue.drone.infrastructure.adapter.in.rest;

import com.aerorescue.drone.domain.model.*;
import com.aerorescue.drone.domain.port.in.*;
import com.aerorescue.drone.domain.port.out.DroneRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Drones", description = "Gestión y telemetría de drones")
public class DroneController {

    private final RegisterDroneUseCase registerUseCase;
    private final ProcessTelemetryUseCase telemetryUseCase;
    private final DroneRepository droneRepository;

    @PostMapping("/drones")
    @Operation(summary = "Registrar nuevo drone — requiere rol DRONE_TECH o ADMIN")
    public ResponseEntity<DroneResponse> register(
            @Valid @RequestBody RegisterDroneRequest req,
            HttpServletRequest http) {
        String role = (String) http.getAttribute("role");
        if (!"DRONE_TECH".equals(role) && !"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Drone drone = registerUseCase.register(req.name(), req.type(), req.lat(), req.lng());
        return ResponseEntity.status(HttpStatus.CREATED).body(DroneResponse.from(drone));
    }

    @PostMapping("/drones/{id}/telemetry")
    @Operation(summary = "Enviar telemetría — simula el hardware del drone en la demo")
    public ResponseEntity<DroneResponse> receiveTelemetry(
            @PathVariable UUID id,
            @Valid @RequestBody TelemetryRequest req) {
        TelemetryData telemetry = new TelemetryData(
            String.valueOf(id), req.lat(), req.lng(), req.altitudeMeters(),
            req.batteryPercentage(), req.status(), req.missionId(), Instant.now()
        );
        Drone drone = telemetryUseCase.process(telemetry);
        return ResponseEntity.ok(DroneResponse.from(drone));
    }

    @GetMapping("/drones")
    @Operation(summary = "Listar todos los drones")
    public ResponseEntity<List<DroneResponse>> getAll() {
        return ResponseEntity.ok(
            droneRepository.findAll().stream().map(DroneResponse::from).toList()
        );
    }

    @GetMapping("/drones/{id}")
    @Operation(summary = "Obtener drone por ID — requiere clearance_level >= 2 para ubicación en tiempo real")
    public ResponseEntity<DroneResponse> getById(@PathVariable UUID id, HttpServletRequest http) {
        int clearance = http.getAttribute("clearanceLevel") != null
            ? (int) http.getAttribute("clearanceLevel") : 0;
        if (clearance < 2) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return droneRepository.findById(id)
            .map(d -> ResponseEntity.ok(DroneResponse.from(d)))
            .orElse(ResponseEntity.notFound().build());
    }

    // DTOs
    public record RegisterDroneRequest(String name, DroneType type, double lat, double lng) {}
    public record TelemetryRequest(double lat, double lng, double altitudeMeters,
                                    int batteryPercentage, String status, String missionId) {}
    public record DroneResponse(UUID id, String name, DroneStatus status, DroneType type,
                                 double lat, double lng, int batteryPercentage,
                                 BatteryStatus batteryStatus, String missionId, Instant lastTelemetryAt) {
        public static DroneResponse from(Drone d) {
            return new DroneResponse(d.getId(), d.getName(), d.getStatus(), d.getType(),
                d.getLat(), d.getLng(), d.getBatteryPercentage(), d.getBatteryStatus(),
                d.getMissionId(), d.getLastTelemetryAt());
        }
    }
}
