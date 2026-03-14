package com.example.productosapi.infrastructure.rest.controller;

import com.example.productosapi.application.usecase.CreateClienteUseCase;
import com.example.productosapi.application.usecase.DeleteClienteUseCase;
import com.example.productosapi.application.usecase.GetClienteUseCase;
import com.example.productosapi.application.usecase.UpdateClienteUseCase;
import com.example.productosapi.domain.model.Cliente;
import com.example.productosapi.infrastructure.persistence.mapper.ClienteMapper;
import com.example.productosapi.infrastructure.rest.dto.EstadoUpdateRequest;
import com.example.productosapi.infrastructure.rest.dto.ClienteRequest;
import com.example.productosapi.infrastructure.rest.dto.ClienteResponse;
import com.example.productosapi.infrastructure.rest.dto.ClienteUpdateRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author kcabezas
 */
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {
    private final CreateClienteUseCase createClienteUseCase;
    private final GetClienteUseCase getClienteUseCase;
    private final UpdateClienteUseCase updateClienteUseCase;
    private final DeleteClienteUseCase deleteClienteUseCase;
    private final ClienteMapper mapper;

    /**
     * Crea un nuevo cliente
     */
    @PostMapping
    public ResponseEntity<ClienteResponse> crearCliente(@Valid @RequestBody ClienteRequest request) {
        Cliente cliente = mapper.toDomain(request);
        Cliente creado = createClienteUseCase.execute(cliente);
        return new ResponseEntity<>(mapper.toResponse(creado), HttpStatus.CREATED);
    }

    /**
     * Obtiene un cliente por su ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> obtenerCliente(@PathVariable UUID id) {
        Cliente cliente = getClienteUseCase.executeById(id);
        return ResponseEntity.ok(mapper.toResponse(cliente));
    }

    /**
     * Obtiene un cliente por su numero de documento
     */
    @GetMapping("/numeroDocumento/{numeroDocumento}")
    public ResponseEntity<ClienteResponse> obtenerClientePorNumeroDocumento(@PathVariable String numeroDocumento) {
        Cliente cliente = getClienteUseCase.executeByNumeroDocumento(numeroDocumento);
        return ResponseEntity.ok(mapper.toResponse(cliente));
    }

    /**
     * Obtiene todos los clientes
     */
    @GetMapping
    public ResponseEntity<List<ClienteResponse>> obtenerTodosLosClientes() {
        List<Cliente> clientes = getClienteUseCase.executeAll();
        return ResponseEntity.ok(mapper.toResponseList(clientes));
    }

    /**
     * Actualiza un cliente existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> actualizarCliente(
            @PathVariable UUID id,
            @Valid @RequestBody ClienteUpdateRequest request) {

        Cliente actualizado = updateClienteUseCase.execute(
                id,
                request.getNombre(),
                request.getApellido(),
                request.getCorreo(),
                request.getTelefono()
        );

        return ResponseEntity.ok(mapper.toResponse(actualizado));
    }

    /**
     * Actualiza el estado de un producto
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<ClienteResponse> actualizarEstadoCliente(
            @PathVariable UUID id,
            @Valid @RequestBody EstadoUpdateRequest request) {

        Cliente actualizado = updateClienteUseCase.executeStateUpdate(id, request.getActivo());
        return ResponseEntity.ok(mapper.toResponse(actualizado));
    }
    
    /**
     * Elimina un producto
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable UUID id) {
        deleteClienteUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

}
