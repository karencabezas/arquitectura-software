package com.example.productosapi.application.usecase;

import com.example.productosapi.domain.model.Cliente;
import com.example.productosapi.domain.service.ClienteService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 *
 * @author kcabezas
 */
@Component
@RequiredArgsConstructor
public class GetClienteUseCase {

    private final ClienteService clienteService;

    public Cliente executeById(UUID id) {
        return clienteService.obtenerClientePorId(id);
    }

    public Cliente executeByNumeroDocumento(String numeroDocumento) {
        return clienteService.obtenerClientePorNumeroDocumento(numeroDocumento);
    }

    public List<Cliente> executeAll() {
        return clienteService.obtenerTodosLosClientes();
    }

}