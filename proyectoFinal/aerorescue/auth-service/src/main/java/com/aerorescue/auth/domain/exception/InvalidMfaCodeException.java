package com.aerorescue.auth.domain.exception;

public class InvalidMfaCodeException extends RuntimeException {
    public InvalidMfaCodeException() {
        super("Invalid or expired MFA code");
    }
}
