package com.aerorescue.emergency.domain.port.in;

import com.aerorescue.emergency.domain.model.*;
import java.util.UUID;

public interface CreateEmergencyUseCase {
    Emergency create(Command cmd);

    record Command(
        EmergencyPriority priority,
        EmergencyType type,
        Location location,
        String description,
        String reportedBy,
        String organizationId,
        // ABAC context from JWT
        String tokenRole,
        String tokenRegion,
        int tokenClearanceLevel
    ) {}
}
