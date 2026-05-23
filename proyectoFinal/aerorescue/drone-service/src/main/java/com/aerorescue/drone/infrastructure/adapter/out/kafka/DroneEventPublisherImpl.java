package com.aerorescue.drone.infrastructure.adapter.out.kafka;

import com.aerorescue.drone.domain.model.*;
import com.aerorescue.drone.domain.port.out.DroneEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DroneEventPublisherImpl implements DroneEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

//    @Override
//    public void publishTelemetry(Drone drone, TelemetryData t) {
//        send("drone.telemetry", drone.getId().toString(), envelope("drone.telemetry", Map.of(
//            "droneId", drone.getId().toString(),
//            "timestamp", t.timestamp().toString(),
//            "location", Map.of("lat", t.lat(), "lng", t.lng(), "altitudeMeters", t.altitudeMeters()),
//            "battery", Map.of("percentage", t.batteryPercentage()),
//            "status", drone.getStatus().name(),
//            "missionId", drone.getMissionId() != null ? drone.getMissionId() : ""
//        )));
//    } TODO

    @Override
    public void publishTelemetry(Drone drone, TelemetryData t) {
        send("drone.telemetry", drone.getId().toString(), envelope("drone.telemetry", Map.of(
                "droneId", drone.getId().toString(),
                "type", drone.getType().name(),
                "timestamp", t.timestamp().toString(),
                "location", Map.of(
                        "lat", t.lat(),
                        "lng", t.lng(),
                        "altitudeMeters", t.altitudeMeters()
                ),
                "battery", Map.of(
                        "percentage", t.batteryPercentage()
                ),
                "status", drone.getStatus().name(),
                "missionId", drone.getMissionId() != null ? drone.getMissionId() : ""
        )));
    }

    @Override
    public void publishBatteryLow(Drone drone, BatteryStatus severity) {
        send("drone.battery.low", drone.getId().toString(), envelope("drone.battery.low", Map.of(
            "droneId", drone.getId().toString(),
            "missionId", drone.getMissionId() != null ? drone.getMissionId() : "",
            "batteryPercentage", drone.getBatteryPercentage(),
            "currentLocation", Map.of("lat", drone.getLat(), "lng", drone.getLng()),
            "severity", severity.name()
        )));
    }

    @Override
    public void publishOffline(Drone drone) {
        send("drone.offline", drone.getId().toString(), envelope("drone.offline", Map.of(
            "droneId", drone.getId().toString(),
            "missionId", drone.getMissionId() != null ? drone.getMissionId() : "",
            "lastSeenAt", drone.getLastTelemetryAt() != null ? drone.getLastTelemetryAt().toString() : "",
            "lastKnownLocation", Map.of("lat", drone.getLat(), "lng", drone.getLng()),
            "lastBatteryPercentage", drone.getBatteryPercentage()
        )));
    }

    @Override
    public void publishStatusChanged(Drone drone, DroneStatus previousStatus) {
        send("drone.status.changed", drone.getId().toString(), envelope("drone.status.changed", Map.of(
            "droneId", drone.getId().toString(),
            "previousStatus", previousStatus.name(),
            "newStatus", drone.getStatus().name(),
            "missionId", drone.getMissionId() != null ? drone.getMissionId() : "",
            "changedAt", Instant.now().toString()
        )));
    }

    private Map<String, Object> envelope(String eventType, Map<String, Object> payload) {
        return Map.of(
            "eventId", UUID.randomUUID().toString(),
            "eventType", eventType,
            "eventVersion", "1.0",
            "occurredAt", Instant.now().toString(),
            "source", "drone-service",
            "correlationId", UUID.randomUUID().toString(),
            "payload", payload
        );
    }

    private void send(String topic, String key, Object payload) {
        kafkaTemplate.send(topic, key, payload)
            .whenComplete((r, ex) -> {
                if (ex != null) log.error("Failed to publish to {}: {}", topic, ex.getMessage());
            });
    }
}
