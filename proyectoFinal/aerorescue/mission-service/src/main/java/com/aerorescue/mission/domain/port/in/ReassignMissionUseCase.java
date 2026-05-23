package com.aerorescue.mission.domain.port.in;

import com.aerorescue.mission.domain.model.Mission;
import java.util.UUID;

public interface ReassignMissionUseCase {
    Mission reassign(UUID missionId, String reason);
}
