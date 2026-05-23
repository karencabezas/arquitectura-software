package com.aerorescue.mission.domain.port.out;

import com.aerorescue.mission.domain.model.Mission;

public interface MissionEventPublisher {
    void publishAssigned(Mission mission);
    void publishReassigned(Mission mission, String previousDroneId, String reason);
    void publishCompleted(Mission mission);
}
