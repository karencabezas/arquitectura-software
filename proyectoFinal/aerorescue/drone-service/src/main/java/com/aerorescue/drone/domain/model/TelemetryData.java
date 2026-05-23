package com.aerorescue.drone.domain.model;

import java.time.Instant;

public record TelemetryData(
    String droneId,
    double lat,
    double lng,
    double altitudeMeters,
    int batteryPercentage,
    String status,
    String missionId,
    Instant timestamp
) {}
