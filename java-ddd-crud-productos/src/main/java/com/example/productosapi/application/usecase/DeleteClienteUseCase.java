package com.example.productosapi.application.usecase;

import com.example.productosapi.domain.service.ClienteService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 *
 * @author kcabezas
 */
@Component
@RequiredArgsConstructor
public class DeleteClienteUseCase {

    private final ClienteService clienteService;

    public void execute(UUID id) {
        clienteService.eliminarCliente(id);
    }
}