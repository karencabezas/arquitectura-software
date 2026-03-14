package com.example.productosapi.application.usecase;

import com.example.productosapi.domain.model.Cliente;
import com.example.productosapi.domain.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class CreateClienteUseCase {

    private final ClienteService clienteService;

    public Cliente execute(Cliente cliente) {
        return clienteService.crearCliente(cliente);
    }
}