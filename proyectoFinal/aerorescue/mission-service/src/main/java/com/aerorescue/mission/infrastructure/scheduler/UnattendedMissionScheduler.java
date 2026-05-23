package com.aerorescue.mission.infrastructure.scheduler;

import com.aerorescue.mission.domain.model.*;
import com.aerorescue.mission.domain.port.out.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Scheduler que evalúa misiones UNATTENDED y las cierra con outcome TIMEOUT
 * cuando se supera el tiempo configurado por prioridad.
 *
 * Timeouts acordados (configurables via application.yml / env vars):
 *   CRITICAL → 5 min
 *   HIGH     → 15 min
 *   MEDIUM   → 30 min
 *   LOW      → 60 min
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UnattendedMissionScheduler {

    private final MissionRepository missionRepository;
    private final MissionEventPublisher eventPublisher;

    @Value("${mission.unattended.timeout.critical:5}")
    private int timeoutCriticalMinutes;

    @Value("${mission.unattended.timeout.high:15}")
    private int timeoutHighMinutes;

    @Value("${mission.unattended.timeout.medium:30}")
    private int timeoutMediumMinutes;

    @Value("${mission.unattended.timeout.low:60}")
    private int timeoutLowMinutes;

    @Scheduled(fixedRate = 60000) // cada 60 segundos
    public void checkUnattendedMissions() {
        List<Mission> unattended = missionRepository.findByStatus(MissionStatus.UNATTENDED);

        for (Mission mission : unattended) {
            if (mission.getUnattendedSince() == null) continue;

            long minutesUnattended = Duration.between(mission.getUnattendedSince(), Instant.now())
                .toMinutes();

            int timeoutMinutes = getTimeout(mission.getPriority());

            if (minutesUnattended >= timeoutMinutes) {
                log.warn("Mission {} TIMEOUT after {} min unattended (limit: {} min) — priority: {}",
                    mission.getId(), minutesUnattended, timeoutMinutes, mission.getPriority());

                Mission closed = mission.toBuilder()
                    .status(MissionStatus.COMPLETED)
                    .outcome(MissionOutcome.TIMEOUT)
                    .completedAt(Instant.now())
                    .closedBy("SYSTEM")
                    .updatedAt(Instant.now())
                    .build();

                missionRepository.save(closed);
                eventPublisher.publishCompleted(closed);
            }
        }
    }

    private int getTimeout(String priority) {
        if (priority == null) return timeoutMediumMinutes;
        return switch (priority.toUpperCase()) {
            case "CRITICAL" -> timeoutCriticalMinutes;
            case "HIGH"     -> timeoutHighMinutes;
            case "LOW"      -> timeoutLowMinutes;
            default         -> timeoutMediumMinutes;
        };
    }
}
