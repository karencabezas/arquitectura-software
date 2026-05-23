package com.aerorescue.emergency.application.usecase;

import com.aerorescue.emergency.domain.exception.EmergencyNotFoundException;
import com.aerorescue.emergency.domain.model.Emergency;
import com.aerorescue.emergency.domain.port.in.GetEmergencyUseCase;
import com.aerorescue.emergency.domain.port.out.EmergencyRepository;
import com.aerorescue.emergency.domain.service.EmergencyDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetEmergencyUseCaseImpl implements GetEmergencyUseCase {

    private final EmergencyRepository repository;
    private final EmergencyDomainService domainService;

    @Override
    public Emergency getById(UUID id, String tokenRole, String tokenRegion, String tokenOrgId) {
        Emergency emergency = repository.findById(id)
            .orElseThrow(() -> new EmergencyNotFoundException(id));
        domainService.validateOrgAccess(tokenRole, tokenOrgId, emergency);
        return emergency;
    }

    @Override
    public List<Emergency> getAll(String tokenRole, String tokenRegion, String tokenOrgId) {
        return switch (tokenRole) {
            case "ADMIN" -> repository.findAll();
            case "SUPERVISOR" -> repository.findByOrganizationId(tokenOrgId);
            default -> "ALL".equals(tokenRegion)
                ? repository.findAll()
                : repository.findByRegion(tokenRegion);
        };
    }
}
