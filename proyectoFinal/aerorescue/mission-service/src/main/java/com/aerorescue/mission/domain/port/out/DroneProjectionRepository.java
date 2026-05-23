package com.aerorescue.mission.domain.port.out;

import com.aerorescue.mission.domain.model.DroneProjection;
import java.util.List;
import java.util.Optional;

public interface DroneProjectionRepository {
    void save(DroneProjection projection);
    Optional<DroneProjection> findByDroneId(String droneId);
    List<DroneProjection> findAvailable();
    List<DroneProjection> findAll();
}
