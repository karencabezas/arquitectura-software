package com.aerorescue.mission.domain.port.in;

import com.aerorescue.mission.domain.model.Mission;

public interface AssignMissionUseCase {
    Mission assign(Command cmd);

    record Command(
        String emergencyId,
        String missionType,
        String priority,
        double emergencyLat,
        double emergencyLng
    ) {}
}
