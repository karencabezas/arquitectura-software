package com.example.productosapi.domain.exception;

import java.util.UUID;

/**
 *
 * @author kcabezas
 */
public class ClienteNotFoundException extends BusinessException {
    
    public ClienteNotFoundException(UUID id) {
        super("No se encontró cliente con ID: " + id);
    }
    
    public ClienteNotFoundException(String numeroDocumento) {
        super("No se encontró cliente con numero de documento: " + numeroDocumento);
    }
    
}
