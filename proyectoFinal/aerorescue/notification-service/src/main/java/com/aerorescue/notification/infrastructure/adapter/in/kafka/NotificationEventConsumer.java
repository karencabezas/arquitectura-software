package com.aerorescue.notification.infrastructure.adapter.in.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Consume todos los eventos relevantes de Kafka
 * y los distribuye al dashboard Angular vía WebSocket (STOMP).
 *
 * Tópicos WebSocket:
 *   /topic/emergencies   → cambios de emergencias
 *   /topic/missions      → asignaciones y reasignaciones
 *   /topic/drones        → telemetría y estado
 *   /topic/alerts        → alertas críticas
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    // ── MISSIONS ──────────────────────────────────────

    @KafkaListener(topics = "mission.assigned", groupId = "notification-service-group")
    public void onMissionAssigned(Map<String, Object> event) {
        push("/topic/missions", event);
        log.debug("Pushed mission.assigned to WebSocket");
    }

    @KafkaListener(topics = "mission.reassigned", groupId = "notification-service-group")
    public void onMissionReassigned(Map<String, Object> event) {
        push("/topic/missions", event);
        log.debug("Pushed mission.reassigned to WebSocket");
    }

    @KafkaListener(topics = "mission.completed", groupId = "notification-service-group")
    public void onMissionCompleted(Map<String, Object> event) {
        push("/topic/missions", event);
        log.debug("Pushed mission.completed to WebSocket");
    }

    // ── ALERTS ────────────────────────────────────────

    @KafkaListener(topics = "alert.created", groupId = "notification-service-group")
    public void onAlertCreated(Map<String, Object> event) {
        push("/topic/alerts", event);
        log.info("Pushed alert to WebSocket");
    }

    // ── DRONES ────────────────────────────────────────

    @KafkaListener(topics = "drone.telemetry", groupId = "notification-service-group")
    public void onDroneTelemetry(Map<String, Object> event) {
        push("/topic/drones", event);
    }

    @KafkaListener(topics = "drone.status.changed", groupId = "notification-service-group")
    public void onDroneStatusChanged(Map<String, Object> event) {
        push("/topic/drones", event);
        log.debug("Pushed drone.status.changed to WebSocket");
    }

    @KafkaListener(topics = "drone.battery.low", groupId = "notification-service-group")
    public void onBatteryLow(Map<String, Object> event) {
        push("/topic/drones", event);
        push("/topic/alerts", event);
    }

    @KafkaListener(topics = "drone.offline", groupId = "notification-service-group")
    public void onDroneOffline(Map<String, Object> event) {
        push("/topic/drones", event);
        push("/topic/alerts", event);
    }

    // ── EMERGENCIES ───────────────────────────────────

    @KafkaListener(topics = "emergency.updated", groupId = "notification-service-group")
    public void onEmergencyUpdated(Map<String, Object> event) {
        push("/topic/emergencies", event);
    }

    @KafkaListener(topics = "emergency.escalated", groupId = "notification-service-group")
    public void onEmergencyEscalated(Map<String, Object> event) {
        push("/topic/emergencies", event);
        push("/topic/alerts", event);
    }

    private void push(String destination, Object payload) {
        try {
            messagingTemplate.convertAndSend(destination, payload);
        } catch (Exception e) {
            log.error("Failed to push to WebSocket {}: {}", destination, e.getMessage());
        }
    }
}
