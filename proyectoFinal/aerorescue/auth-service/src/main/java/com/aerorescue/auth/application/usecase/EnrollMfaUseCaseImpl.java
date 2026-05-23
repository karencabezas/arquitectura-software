package com.aerorescue.auth.application.usecase;

import com.aerorescue.auth.domain.model.User;
import com.aerorescue.auth.domain.port.in.EnrollMfaUseCase;
import com.aerorescue.auth.domain.port.out.UserRepository;
import com.aerorescue.auth.domain.service.TotpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EnrollMfaUseCaseImpl implements EnrollMfaUseCase {

    private final TotpService totpService;
    private final UserRepository userRepository;

    @Override
    public EnrollResult enroll(String userId) {
        User user = userRepository.findById(UUID.fromString(userId))
            .orElseThrow(() -> new RuntimeException("User not found"));

        String secret = totpService.generateSecret();
        String qrUrl = totpService.generateQrUrl(user.getUsername(), secret);

        // Persistir secret en el usuario
        User updated = User.builder()
            .id(user.getId())
            .username(user.getUsername())
            .password(user.getPassword())
            .role(user.getRole())
            .region(user.getRegion())
            .clearanceLevel(user.getClearanceLevel())
            .allowedMissionTypes(user.getAllowedMissionTypes())
            .organizationId(user.getOrganizationId())
            .mfaSecret(secret)
            .mfaEnabled(true)
            .active(user.isActive())
            .build();

        userRepository.save(updated);
        return new EnrollResult(secret, qrUrl);
    }
}
