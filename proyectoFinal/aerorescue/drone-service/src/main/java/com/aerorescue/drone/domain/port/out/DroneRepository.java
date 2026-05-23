package com.aerorescue.drone.domain.port.out;

import com.aerorescue.drone.domain.model.*;
import java.util.*;

public interface DroneRepository {
    Drone save(Drone drone);
    Optional<Drone> findById(UUID id);
    Optional<Drone> findByStringId(String id);
    List<Drone> findAll();
    List<Drone> findByStatus(DroneStatus status);
    List<Drone> findMonitorable();
}
