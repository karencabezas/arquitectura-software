package com.aerorescue.mission.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "drone_projections")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DroneProjectionEntity {
    @Id
    @Column(name = "drone_id")
    private String droneId;
    private String status;
    private String type;
    private double lat;
    private double lng;
    @Column(name = "battery_percentage") private int batteryPercentage;
    @Column(name = "mission_id") private String missionId;
    @Column(name = "last_updated_at") private Instant lastUpdatedAt;
}
