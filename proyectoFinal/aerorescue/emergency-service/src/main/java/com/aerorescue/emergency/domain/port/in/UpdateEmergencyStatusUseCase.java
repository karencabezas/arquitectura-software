package com.aerorescue.emergency.domain.port.in;

import com.aerorescue.emergency.domain.model.Emergency;
import com.aerorescue.emergency.domain.model.EmergencyStatus;
import java.util.UUID;

public interface UpdateEmergencyStatusUseCase {
    Emergency updateStatus(UUID emergencyId, EmergencyStatus newStatus,
                           String updatedBy, String tokenRole, String tokenOrgId);
}
