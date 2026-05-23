package com.aerorescue.drone.domain.port.in;

import com.aerorescue.drone.domain.model.*;

public interface ProcessTelemetryUseCase {
    Drone process(TelemetryData telemetry);
}
