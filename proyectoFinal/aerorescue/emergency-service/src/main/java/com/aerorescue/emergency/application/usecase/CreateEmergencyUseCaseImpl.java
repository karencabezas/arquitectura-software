package com.aerorescue.emergency.application.usecase;

import com.aerorescue.emergency.domain.model.Emergency;
import com.aerorescue.emergency.domain.model.EmergencyStatus;
import com.aerorescue.emergency.domain.port.in.CreateEmergencyUseCase;
import com.aerorescue.emergency.domain.port.out.EmergencyEventPublisher;
import com.aerorescue.emergency.domain.port.out.EmergencyRepository;
import com.aerorescue.emergency.domain.service.EmergencyDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateEmergencyUseCaseImpl implements CreateEmergencyUseCase {

    private final EmergencyRepository repository;
    private final EmergencyEventPublisher eventPublisher;
    private final EmergencyDomainService domainService;

    @Override
    public Emergency create(Command cmd) {
        Emergency emergency = Emergency.builder()
            .id(UUID.randomUUID())
            .status(EmergencyStatus.PENDING)
            .priority(cmd.priority())
            .type(cmd.type())
            .location(cmd.location())
            .description(cmd.description())
            .reportedBy(cmd.reportedBy())
            .organizationId(cmd.organizationId())
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        // ABAC validation — domain service
        domainService.validateCreateAbac(
            cmd.tokenRole(), cmd.tokenRegion(),
            cmd.tokenClearanceLevel(), emergency
        );

        Emergency saved = repository.save(emergency);
        eventPublisher.publishCreated(saved);
        return saved;
    }
}
