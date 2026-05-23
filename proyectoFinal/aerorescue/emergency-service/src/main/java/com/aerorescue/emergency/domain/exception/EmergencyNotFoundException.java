package com.aerorescue.emergency.domain.exception;

import java.util.UUID;

public class EmergencyNotFoundException extends RuntimeException {
    public EmergencyNotFoundException(UUID id) {
        super("Emergency not found: " + id);
    }
}
