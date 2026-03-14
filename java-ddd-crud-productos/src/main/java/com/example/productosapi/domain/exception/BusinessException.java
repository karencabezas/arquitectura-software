package com.example.productosapi.domain.exception;

/**
 * Excepción base para errores de negocio en el dominio
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}