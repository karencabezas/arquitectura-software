package com.aerorescue.alert.domain.model;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class Alert {
    private UUID id;
    private AlertType type;
    private AlertSeverity severity;
    private String relatedEntityType;
    private String relatedEntityId;
    private String missionId;
    private String emergencyId;
    private String message;
    private boolean autoResolvable;
    private Instant createdAt;
}
