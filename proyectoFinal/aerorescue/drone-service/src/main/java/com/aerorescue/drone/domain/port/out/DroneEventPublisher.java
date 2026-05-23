package com.aerorescue.drone.domain.port.out;

import com.aerorescue.drone.domain.model.*;

public interface DroneEventPublisher {
    void publishTelemetry(Drone drone, TelemetryData telemetry);
    void publishBatteryLow(Drone drone, BatteryStatus severity);
    void publishOffline(Drone drone);
    void publishStatusChanged(Drone drone, DroneStatus previousStatus);
}
