package com.aerorescue.emergency.application.usecase;

import com.aerorescue.emergency.domain.exception.EmergencyNotFoundException;
import com.aerorescue.emergency.domain.model.Emergency;
import com.aerorescue.emergency.domain.model.EmergencyStatus;
import com.aerorescue.emergency.domain.port.in.UpdateEmergencyStatusUseCase;
import com.aerorescue.emergency.domain.port.out.EmergencyEventPublisher;
import com.aerorescue.emergency.domain.port.out.EmergencyRepository;
import com.aerorescue.emergency.domain.service.EmergencyDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateEmergencyStatusUseCaseImpl implements UpdateEmergencyStatusUseCase {

    private final EmergencyRepository repository;
    private final EmergencyEventPublisher eventPublisher;
    private final EmergencyDomainService domainService;

    @Override
    public Emergency updateStatus(UUID emergencyId, EmergencyStatus newStatus,
                                   String updatedBy, String tokenRole, String tokenOrgId) {
        Emergency existing = repository.findById(emergencyId)
            .orElseThrow(() -> new EmergencyNotFoundException(emergencyId));

        domainService.validateOrgAccess(tokenRole, tokenOrgId, existing);

        EmergencyStatus previousStatus = existing.getStatus();
        Emergency updated = existing.toBuilder()
            .status(newStatus)
            .build();

        Emergency saved = repository.save(updated);
        eventPublisher.publishUpdated(saved, previousStatus, updatedBy);
        return saved;
    }
}
