package com.aerorescue.drone.infrastructure.adapter.out.persistence.entity;

import com.aerorescue.drone.domain.model.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "drones")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DroneEntity {
    @Id private UUID id;
    @Column(nullable = false) private String name;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private DroneStatus status;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private DroneType type;
    private double lat;
    private double lng;
    @Column(name = "altitude_meters") private double altitudeMeters;
    @Column(name = "battery_percentage") private int batteryPercentage;
    @Enumerated(EnumType.STRING) @Column(name = "battery_status") private BatteryStatus batteryStatus;
    @Column(name = "mission_id") private String missionId;
    @Column(name = "last_telemetry_at") private Instant lastTelemetryAt;
    @Column(name = "created_at") private Instant createdAt;
    @Column(name = "updated_at") private Instant updatedAt;
}
