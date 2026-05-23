package com.aerorescue.mission.domain.port.out;

import com.aerorescue.mission.domain.model.Mission;
import com.aerorescue.mission.domain.model.MissionStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MissionRepository {
    Mission save(Mission mission);
    Optional<Mission> findById(UUID id);
    Optional<Mission> findActiveByEmergencyId(String emergencyId);
    List<Mission> findByStatus(MissionStatus status);
    List<Mission> findAll();
}
