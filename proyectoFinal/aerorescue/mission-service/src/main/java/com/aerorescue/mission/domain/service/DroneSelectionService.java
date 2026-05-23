package com.aerorescue.mission.domain.service;

import com.aerorescue.mission.domain.model.DroneProjection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Algoritmo de selección de drones acordado:
 *
 * SCORE = (0.35 × score_battery)
 *       + (0.30 × score_distance_haversine)
 *       + (0.25 × score_mission_type)
 *       + (0.10 × score_availability)
 *
 * Descarte automático:
 *   - battery < 20%         → excluido
 *   - distance > maxDistKm  → excluido
 *   - type incompatible     → excluido
 *   - score total < minScore → ningún candidato
 */
@Slf4j
@Service
public class DroneSelectionService {

    @Value("${drone-selection.weights.battery:0.35}")
    private double weightBattery;

    @Value("${drone-selection.weights.distance:0.30}")
    private double weightDistance;

    @Value("${drone-selection.weights.mission-type:0.25}")
    private double weightMissionType;

    @Value("${drone-selection.weights.availability:0.10}")
    private double weightAvailability;

    @Value("${drone-selection.min-score:40}")
    private double minScore;

    @Value("${drone-selection.max-distance-km:50}")
    private double maxDistanceKm;

    public Optional<DroneProjection> selectBest(
            List<DroneProjection> candidates,
            String missionType,
            double emergencyLat,
            double emergencyLng) {

        return candidates.stream()
            .filter(d -> d.getBatteryPercentage() >= 20)
            .filter(d -> haversineKm(d.getLat(), d.getLng(), emergencyLat, emergencyLng) <= maxDistanceKm)
                .filter(d -> !isIncompatible(d.getType(), missionType))
            .map(d -> new ScoredDrone(d, calculateScore(d, missionType, emergencyLat, emergencyLng)))
            .filter(sd -> sd.score() >= minScore)
            .max(Comparator.comparingDouble(ScoredDrone::score))
            .map(sd -> {
                log.info("Selected drone {} with score {:.1f}", sd.drone().getDroneId(), sd.score());
                return sd.drone();
            });
    }

    private double calculateScore(DroneProjection drone, String missionType,
                                   double emergencyLat, double emergencyLng) {
        double scoreBattery    = scoreBattery(drone.getBatteryPercentage());
        double scoreDistance   = scoreDistance(drone.getLat(), drone.getLng(), emergencyLat, emergencyLng);
        double scoreMissionType = scoreMissionType(drone.getType(), missionType);
        double scoreAvailability = scoreAvailability(drone.getStatus());

        return (weightBattery * scoreBattery)
             + (weightDistance * scoreDistance)
             + (weightMissionType * scoreMissionType)
             + (weightAvailability * scoreAvailability);
    }

    // ── Score parciales ─────────────────────────────

    private double scoreBattery(int battery) {
        if (battery >= 80) return 100;
        if (battery >= 50) return 75;
        if (battery >= 30) return 50;
        if (battery >= 20) return 20;
        return 0;
    }

    private double scoreDistance(double droneLat, double droneLng,
                                  double emergencyLat, double emergencyLng) {
        double distKm = haversineKm(droneLat, droneLng, emergencyLat, emergencyLng);
        return Math.max(0, 100.0 * (1.0 - distKm / maxDistanceKm));
    }

    private double scoreMissionType(String droneType, String missionType) {

        if (droneType == null) {
            return 50;
        }

        if (droneType.equals(missionType)) return 100;

        if (isCompatible(droneType, missionType)) return 60;

        return 0;
    }

    private double scoreAvailability(String status) {
        return switch (status) {
            case "AVAILABLE" -> 100;
            case "RETURNING" -> 60;
            case "ON_MISSION" -> 30;
            default -> 0;
        };
    }

    // ── Compatibilidad de tipos ──────────────────────


        // RESCUE puede hacer SEARCH; MEDICAL solo MEDICAL; FIRE solo FIRE; SEARCH puede hacer RESCUE
    private boolean isIncompatible(String droneType, String missionType) {

            if (droneType == null) {
                return true;
            }
        log.info("Checking compatibility droneType={} missionType={}",droneType,missionType);
            return switch (missionType) {
                case "MEDICAL" -> !"MEDICAL".equals(droneType);
                case "FIRE"    -> !"FIRE".equals(droneType);
                case "RESCUE"  -> !"RESCUE".equals(droneType) && !"SEARCH".equals(droneType);
                case "SEARCH"  -> !"SEARCH".equals(droneType) && !"RESCUE".equals(droneType);
                default -> true;
            };
        }

    private boolean isCompatible(String droneType, String missionType) {
        return !isIncompatible(droneType, missionType);
    }

    // ── Fórmula Haversine ────────────────────────────

    private double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private record ScoredDrone(DroneProjection drone, double score) {}
}
