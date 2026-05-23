package com.aerorescue.emergency.domain.exception;

public class AbacViolationException extends RuntimeException {
    public AbacViolationException(String message) {
        super(message);
    }
}
