package com.example.productosapi.application.usecase;

import com.example.productosapi.domain.model.Cliente;
import com.example.productosapi.domain.service.ClienteService;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 *
 * @author kcabezas
 */
@Component
@RequiredArgsConstructor
public class UpdateClienteUseCase {

    private final ClienteService clienteService;

    public Cliente execute(UUID id, String nombre, String apellido,String correo,String telefono) {
        return clienteService.actualizarCliente(id, nombre, apellido, correo, telefono);
    }


    public Cliente executeStateUpdate(UUID id, Boolean activo) {
        return clienteService.actualizarEstadoCliente(id, activo);
    }

}