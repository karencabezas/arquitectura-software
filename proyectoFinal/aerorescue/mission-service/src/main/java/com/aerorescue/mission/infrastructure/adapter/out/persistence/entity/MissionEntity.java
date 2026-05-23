package com.aerorescue.mission.infrastructure.adapter.out.persistence.entity;

import com.aerorescue.mission.domain.model.MissionOutcome;
import com.aerorescue.mission.domain.model.MissionStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "missions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MissionEntity {
    @Id private UUID id;
    @Column(name = "emergency_id") private String emergencyId;
    @Column(name = "drone_id") private String droneId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private MissionStatus status;
    @Enumerated(EnumType.STRING) private MissionOutcome outcome;
    @Column(name = "mission_type") private String missionType;
    private String priority;
    @Column(name = "assigned_at") private Instant assignedAt;
    @Column(name = "unattended_since") private Instant unattendedSince;
    @Column(name = "completed_at") private Instant completedAt;
    @Column(name = "closed_by") private String closedBy;
    @Column(name = "created_at") private Instant createdAt;
    @Column(name = "updated_at") private Instant updatedAt;
}
