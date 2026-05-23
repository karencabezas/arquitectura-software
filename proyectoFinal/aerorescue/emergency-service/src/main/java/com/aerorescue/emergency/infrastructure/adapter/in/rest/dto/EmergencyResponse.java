package com.aerorescue.emergency.infrastructure.adapter.in.rest.dto;

import com.aerorescue.emergency.domain.model.Emergency;
import com.aerorescue.emergency.domain.model.EmergencyPriority;
import com.aerorescue.emergency.domain.model.EmergencyStatus;
import com.aerorescue.emergency.domain.model.EmergencyType;
import java.time.Instant;
import java.util.UUID;

public record EmergencyResponse(
    UUID id,
    EmergencyStatus status,
    EmergencyPriority priority,
    EmergencyType type,
    String address,
    String region,
    double lat,
    double lng,
    String description,
    String reportedBy,
    String organizationId,
    Instant createdAt,
    Instant updatedAt
) {
    public static EmergencyResponse from(Emergency e) {
        return new EmergencyResponse(
            e.getId(), e.getStatus(), e.getPriority(), e.getType(),
            e.getLocation().getAddress(), e.getLocation().getRegion(),
            e.getLocation().getLat(), e.getLocation().getLng(),
            e.getDescription(), e.getReportedBy(), e.getOrganizationId(),
            e.getCreatedAt(), e.getUpdatedAt()
        );
    }
}
