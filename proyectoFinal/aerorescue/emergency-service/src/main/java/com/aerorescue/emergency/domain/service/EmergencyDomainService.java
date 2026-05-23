package com.aerorescue.emergency.domain.service;

import com.aerorescue.emergency.domain.exception.AbacViolationException;
import com.aerorescue.emergency.domain.model.*;
import org.springframework.stereotype.Service;

@Service
public class EmergencyDomainService {

    /**
     * ABAC-01: OPERATOR solo puede crear emergencias en su región.
     * ABAC-02: clearance_level >= 3 requerido para crear emergencias CRITICAL.
     */
    public void validateCreateAbac(String role, String tokenRegion,
                                   int clearanceLevel, Emergency emergency) {
        // ABAC-01
        if ("OPERATOR".equals(role) && !"ALL".equals(tokenRegion)) {
            if (!tokenRegion.equals(emergency.getLocation().getRegion())) {
                throw new AbacViolationException(
                    "ABAC-01: Operator can only create emergencies in their region: " + tokenRegion
                );
            }
        }
        // ABAC-02
        if (EmergencyPriority.CRITICAL.equals(emergency.getPriority()) && clearanceLevel < 3) {
            throw new AbacViolationException(
                "ABAC-02: clearance_level >= 3 required to create CRITICAL emergencies"
            );
        }
    }

    /**
     * ABAC-03: SUPERVISOR solo puede ver/modificar emergencias de su org.
     */
    public void validateOrgAccess(String role, String tokenOrgId, Emergency emergency) {
        if ("SUPERVISOR".equals(role)) {
            if (!tokenOrgId.equals(emergency.getOrganizationId())) {
                throw new AbacViolationException(
                    "ABAC-03: Supervisor can only manage emergencies of their organization"
                );
            }
        }
    }
}
