package com.aerorescue.emergency.domain.port.in;

import com.aerorescue.emergency.domain.model.Emergency;
import java.util.UUID;

public interface EscalateEmergencyUseCase {
    Emergency escalate(UUID emergencyId, String escalatedBy, int tokenClearanceLevel);
}
