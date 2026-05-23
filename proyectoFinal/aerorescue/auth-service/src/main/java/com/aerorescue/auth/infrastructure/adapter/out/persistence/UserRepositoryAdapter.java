package com.aerorescue.auth.infrastructure.adapter.out.persistence;

import com.aerorescue.auth.domain.model.User;
import com.aerorescue.auth.domain.port.out.UserRepository;
import com.aerorescue.auth.infrastructure.adapter.out.persistence.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(this::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity entity = toEntity(user);
        return toDomain(jpaRepository.save(entity));
    }

    private User toDomain(UserEntity e) {
        List<String> missionTypes = e.getAllowedMissionTypes() != null
            ? Arrays.asList(e.getAllowedMissionTypes().split(","))
            : List.of();
        return User.builder()
            .id(e.getId())
            .username(e.getUsername())
            .password(e.getPassword())
            .role(e.getRole())
            .region(e.getRegion())
            .clearanceLevel(e.getClearanceLevel())
            .allowedMissionTypes(missionTypes)
            .organizationId(e.getOrganizationId())
            .mfaSecret(e.getMfaSecret())
            .mfaEnabled(e.isMfaEnabled())
            .active(e.isActive())
            .build();
    }

    private UserEntity toEntity(User u) {
        String missionTypes = u.getAllowedMissionTypes() != null
            ? String.join(",", u.getAllowedMissionTypes())
            : null;
        return UserEntity.builder()
            .id(u.getId())
            .username(u.getUsername())
            .password(u.getPassword())
            .role(u.getRole())
            .region(u.getRegion())
            .clearanceLevel(u.getClearanceLevel())
            .allowedMissionTypes(missionTypes)
            .organizationId(u.getOrganizationId())
            .mfaSecret(u.getMfaSecret())
            .mfaEnabled(u.isMfaEnabled())
            .active(u.isActive())
            .build();
    }
}
