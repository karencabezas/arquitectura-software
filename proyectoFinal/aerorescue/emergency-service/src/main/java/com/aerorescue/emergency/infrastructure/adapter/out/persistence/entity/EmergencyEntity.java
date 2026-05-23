package com.aerorescue.emergency.infrastructure.adapter.out.persistence.entity;

import com.aerorescue.emergency.domain.model.EmergencyPriority;
import com.aerorescue.emergency.domain.model.EmergencyStatus;
import com.aerorescue.emergency.domain.model.EmergencyType;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "emergencies")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmergencyEntity {
    @Id private UUID id;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private EmergencyStatus status;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private EmergencyPriority priority;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private EmergencyType type;
    private String address;
    private String region;
    private double lat;
    private double lng;
    @Column(length = 1000) private String description;
    @Column(name = "reported_by") private String reportedBy;
    @Column(name = "organization_id") private String organizationId;
    @Column(name = "created_at") private Instant createdAt;
    @Column(name = "updated_at") private Instant updatedAt;
}
