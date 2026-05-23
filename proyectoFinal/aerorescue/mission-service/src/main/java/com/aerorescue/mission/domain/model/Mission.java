package com.aerorescue.mission.domain.model;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
public class Mission {
    private UUID id;
    private String emergencyId;
    private String droneId;
    private MissionStatus status;
    private MissionOutcome outcome;
    private String missionType;
    private String priority;
    private Instant assignedAt;
    private Instant unattendedSince;
    private Instant completedAt;
    private String closedBy;
    private Instant createdAt;
    private Instant updatedAt;
}
