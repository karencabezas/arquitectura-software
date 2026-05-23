package com.aerorescue.emergency.domain.port.in;

import com.aerorescue.emergency.domain.model.Emergency;
import java.util.List;
import java.util.UUID;

public interface GetEmergencyUseCase {
    Emergency getById(UUID id, String tokenRole, String tokenRegion, String tokenOrgId);
    List<Emergency> getAll(String tokenRole, String tokenRegion, String tokenOrgId);
}
