package com.aerorescue.mission.infrastructure.adapter.in.kafka;

import com.aerorescue.mission.domain.model.DroneProjection;
import com.aerorescue.mission.domain.port.in.AssignMissionUseCase;
import com.aerorescue.mission.domain.port.in.ReassignMissionUseCase;
import com.aerorescue.mission.domain.port.out.DroneProjectionRepository;
import com.aerorescue.mission.domain.port.out.MissionRepository;
import com.aerorescue.mission.domain.model.MissionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmergencyEventConsumer {

    private final AssignMissionUseCase assignMissionUseCase;

    @KafkaListener(topics = "emergency.created", groupId = "mission-service-group")
    public void onEmergencyCreated(Map<String, Object> event) {
        try {
            Map<String, Object> payload = (Map<String, Object>) event.get("payload");
            Map<String, Object> location = (Map<String, Object>) payload.get("location");

            assignMissionUseCase.assign(new AssignMissionUseCase.Command(
                (String) payload.get("emergencyId"),
                (String) payload.get("type"),
                (String) payload.get("priority"),
                ((Number) location.get("lat")).doubleValue(),
                ((Number) location.get("lng")).doubleValue()
            ));
            log.info("Mission assigned for emergency {}", payload.get("emergencyId"));
        } catch (Exception e) {
            log.error("Error processing emergency.created: {}", e.getMessage(), e);
        }
    }
}
