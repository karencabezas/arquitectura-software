package com.aerorescue.drone.domain.service;

import com.aerorescue.drone.domain.model.BatteryStatus;
import com.aerorescue.drone.domain.model.Drone;
import org.springframework.stereotype.Service;

@Service
public class BatteryMonitorService {

    // Umbrales acordados: WARNING < 20%, CRITICAL < 5%
    private static final int WARNING_THRESHOLD = 20;
    private static final int CRITICAL_THRESHOLD = 5;

    public BatteryAlertResult evaluate(Drone drone) {
        int battery = drone.getBatteryPercentage();
        if (battery < CRITICAL_THRESHOLD) {
            return new BatteryAlertResult(true, BatteryStatus.CRITICAL, battery);
        }
        if (battery < WARNING_THRESHOLD) {
            return new BatteryAlertResult(true, BatteryStatus.LOW, battery);
        }
        return new BatteryAlertResult(false, BatteryStatus.NORMAL, battery);
    }

    public record BatteryAlertResult(
        boolean shouldAlert,
        BatteryStatus severity,
        int batteryPercentage
    ) {}
}
