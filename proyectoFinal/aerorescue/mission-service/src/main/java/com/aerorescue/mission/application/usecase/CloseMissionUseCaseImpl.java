package com.aerorescue.mission.application.usecase;

import com.aerorescue.mission.domain.exception.AbacViolationException;
import com.aerorescue.mission.domain.model.*;
import com.aerorescue.mission.domain.port.in.CloseMissionUseCase;
import com.aerorescue.mission.domain.port.out.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloseMissionUseCaseImpl implements CloseMissionUseCase {

    private final MissionRepository missionRepository;
    private final MissionEventPublisher eventPublisher;

    @Override
    public Mission close(UUID missionId, MissionOutcome outcome, String closedBy, int tokenClearanceLevel) {
        // ABAC-05: reasignación manual requiere clearance_level >= 3
        if (tokenClearanceLevel < 2) {
            throw new AbacViolationException("clearance_level >= 2 required to close missions");
        }

        Mission mission = missionRepository.findById(missionId)
            .orElseThrow(() -> new RuntimeException("Mission not found: " + missionId));

        Mission completed = mission.toBuilder()
            .status(MissionStatus.COMPLETED)
            .outcome(outcome)
            .closedBy(closedBy)
            .completedAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        Mission saved = missionRepository.save(completed);
        eventPublisher.publishCompleted(saved);
        log.info("Mission {} closed with outcome {} by {}", missionId, outcome, closedBy);
        return saved;
    }
}
