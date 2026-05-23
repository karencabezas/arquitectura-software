package com.aerorescue.emergency.domain.port.out;

import com.aerorescue.emergency.domain.model.Emergency;
import com.aerorescue.emergency.domain.model.EmergencyStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmergencyRepository {
    Emergency save(Emergency emergency);
    Optional<Emergency> findById(UUID id);
    List<Emergency> findAll();
    List<Emergency> findByRegion(String region);
    List<Emergency> findByOrganizationId(String orgId);
}
