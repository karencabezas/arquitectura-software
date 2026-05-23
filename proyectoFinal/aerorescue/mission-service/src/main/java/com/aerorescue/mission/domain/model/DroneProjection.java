package com.aerorescue.mission.domain.model;

import lombok.*;
import java.time.Instant;

@Getter
@Builder(toBuilder = true)
public class DroneProjection {
    private String droneId;
    private String status;       // AVAILABLE, ON_MISSION, RETURNING, OFFLINE, INACTIVE, MAINTENANCE
    private String type;         // RESCUE, MEDICAL, SEARCH, FIRE
    private double lat;
    private double lng;
    private int batteryPercentage;
    private String missionId;
    private Instant lastUpdatedAt;

    public boolean isAvailableForAssignment() {
        return "AVAILABLE".equals(status) && batteryPercentage >= 20;
    }

    public boolean isCompatibleWith(String missionType) {
        if ("ADMIN".equals(type)) return true;
        return type.equals(missionType);
    }
}
