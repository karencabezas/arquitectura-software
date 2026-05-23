package com.aerorescue.drone.domain.port.in;

import com.aerorescue.drone.domain.model.*;
import java.util.List;
import java.util.UUID;

public interface RegisterDroneUseCase {
    Drone register(String name, DroneType type, double lat, double lng);
}
