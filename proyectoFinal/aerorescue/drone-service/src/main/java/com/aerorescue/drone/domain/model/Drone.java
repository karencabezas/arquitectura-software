package com.aerorescue.drone.domain.model;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
public class Drone {
    private UUID id;
    private String name;
    private DroneStatus status;
    private DroneType type;
    private double lat;
    private double lng;
    private double altitudeMeters;
    private int batteryPercentage;
    private BatteryStatus batteryStatus;
    private String missionId;
    private Instant lastTelemetryAt;
    private Instant createdAt;
    private Instant updatedAt;

    public BatteryStatus computeBatteryStatus() {
        if (batteryPercentage < 5) return BatteryStatus.CRITICAL;
        if (batteryPercentage < 20) return BatteryStatus.LOW;
        return BatteryStatus.NORMAL;
    }

    public boolean isMonitorable() {
        return status == DroneStatus.AVAILABLE
            || status == DroneStatus.ON_MISSION
            || status == DroneStatus.RETURNING;
    }

    public Drone applyTelemetry(TelemetryData t) {
        int newBattery = t.batteryPercentage();

        return this.toBuilder()
                .lat(t.lat())
                .lng(t.lng())
                .altitudeMeters(t.altitudeMeters())
                .batteryPercentage(newBattery)
                .batteryStatus(
                        newBattery < 5
                                ? BatteryStatus.CRITICAL
                                : newBattery < 20
                                ? BatteryStatus.LOW
                                : BatteryStatus.NORMAL
                )
                .status(DroneStatus.valueOf(t.status()))
                .missionId(t.missionId())
                .lastTelemetryAt(t.timestamp())
                .updatedAt(Instant.now())
                .build();
    }
}
