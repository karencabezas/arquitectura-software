package com.aerorescue.emergency.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
public class Emergency {
    private UUID id;
    private EmergencyStatus status;
    private EmergencyPriority priority;
    private EmergencyType type;
    private Location location;
    private String description;
    private String reportedBy;
    private String organizationId;
    private Instant createdAt;
    private Instant updatedAt;

    public Emergency escalate() {
        EmergencyPriority newPriority = priority.escalate();
        return this.toBuilder()
            .priority(newPriority)
            .status(EmergencyStatus.CRITICAL)
            .updatedAt(Instant.now())
            .build();
    }

    public Emergency assignDrone() {
        return this.toBuilder()
            .status(EmergencyStatus.ASSIGNED)
            .updatedAt(Instant.now())
            .build();
    }

    public Emergency resolve() {
        return this.toBuilder()
            .status(EmergencyStatus.RESOLVED)
            .updatedAt(Instant.now())
            .build();
    }
}
