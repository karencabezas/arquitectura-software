package com.aerorescue.emergency.domain.model;

public enum EmergencyPriority {
    LOW, MEDIUM, HIGH, CRITICAL;

    public EmergencyPriority escalate() {
        return switch (this) {
            case LOW -> MEDIUM;
            case MEDIUM -> HIGH;
            case HIGH, CRITICAL -> CRITICAL;
        };
    }

    public int getLevel() {
        return switch (this) {
            case LOW -> 1;
            case MEDIUM -> 2;
            case HIGH -> 3;
            case CRITICAL -> 4;
        };
    }
}
