package com.aerorescue.emergency.application.usecase;

import com.aerorescue.emergency.domain.exception.AbacViolationException;
import com.aerorescue.emergency.domain.exception.EmergencyNotFoundException;
import com.aerorescue.emergency.domain.model.Emergency;
import com.aerorescue.emergency.domain.model.EmergencyPriority;
import com.aerorescue.emergency.domain.port.in.EscalateEmergencyUseCase;
import com.aerorescue.emergency.domain.port.out.EmergencyEventPublisher;
import com.aerorescue.emergency.domain.port.out.EmergencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EscalateEmergencyUseCaseImpl implements EscalateEmergencyUseCase {

    private final EmergencyRepository repository;
    private final EmergencyEventPublisher eventPublisher;

    @Override
    public Emergency escalate(UUID emergencyId, String escalatedBy, int tokenClearanceLevel) {
        // ABAC-02: clearance_level >= 3 requerido para escalar
        if (tokenClearanceLevel < 3) {
            throw new AbacViolationException(
                "ABAC-02: clearance_level >= 3 required to escalate emergencies"
            );
        }

        Emergency existing = repository.findById(emergencyId)
            .orElseThrow(() -> new EmergencyNotFoundException(emergencyId));

        EmergencyPriority previousPriority = existing.getPriority();
        Emergency escalated = existing.escalate();
        Emergency saved = repository.save(escalated);
        eventPublisher.publishEscalated(saved, previousPriority, escalatedBy);
        return saved;
    }
}
