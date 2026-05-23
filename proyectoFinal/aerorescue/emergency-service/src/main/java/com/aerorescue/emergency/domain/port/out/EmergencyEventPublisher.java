package com.aerorescue.emergency.domain.port.out;

import com.aerorescue.emergency.domain.model.Emergency;
import com.aerorescue.emergency.domain.model.EmergencyPriority;
import com.aerorescue.emergency.domain.model.EmergencyStatus;

public interface EmergencyEventPublisher {
    void publishCreated(Emergency emergency);
    void publishUpdated(Emergency emergency, EmergencyStatus previousStatus, String updatedBy);
    void publishEscalated(Emergency emergency, EmergencyPriority previousPriority, String escalatedBy);
}
