package com.aerorescue.mission.domain.port.in;

import com.aerorescue.mission.domain.model.Mission;
import com.aerorescue.mission.domain.model.MissionOutcome;
import java.util.UUID;

public interface CloseMissionUseCase {
    Mission close(UUID missionId, MissionOutcome outcome, String closedBy, int tokenClearanceLevel);
}
