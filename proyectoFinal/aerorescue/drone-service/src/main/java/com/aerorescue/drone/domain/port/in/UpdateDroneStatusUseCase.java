package com.aerorescue.drone.domain.port.in;

import com.aerorescue.drone.domain.model.*;
import java.util.UUID;

public interface UpdateDroneStatusUseCase {
    Drone updateStatus(UUID droneId, DroneStatus newStatus, String missionId);
}
