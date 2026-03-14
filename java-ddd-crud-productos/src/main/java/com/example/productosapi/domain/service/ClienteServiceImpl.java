package com.example.productosapi.domain.service;

import com.example.productosapi.domain.exception.BusinessException;
import com.example.productosapi.domain.exception.ClienteNotFoundException;
import com.example.productosapi.domain.exception.ProductoNotFoundException;
import com.example.productosapi.domain.model.Cliente;
import com.example.productosapi.domain.repository.ClienteRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author kcabezas
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ClienteServiceImpl implements ClienteService {
    
    private final ClienteRepository clienteRepository;

    @Override
    public Cliente crearCliente(Cliente cliente) {
        if (clienteRepository.existsByNumeroDocumento(cliente.getNumeroDocumento())) {
            throw new BusinessException("Ya existe un cliente con el número de documento: " + cliente.getNumeroDocumento());
        }

        return clienteRepository.save(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente obtenerClientePorId(UUID id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente obtenerClientePorNumeroDocumento(String numeroDocumento) {
        return clienteRepository.findByNumeroDocumento(numeroDocumento)
                .orElseThrow(() -> new ClienteNotFoundException(numeroDocumento));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> obtenerTodosLosClientes() {
        return clienteRepository.findAll();
    }

    @Override
    public void eliminarCliente(UUID id) {
        if (!clienteRepository.findById(id).isPresent()) {
            throw new ProductoNotFoundException(id);
        }

        clienteRepository.deleteById(id);
    }

    @Override
    public Cliente actualizarCliente(UUID id, String nombre, String apellido,String correo,String telefono) {
        Cliente clienteExistente = obtenerClientePorId(id);

        Cliente clienteActualizado = clienteExistente.actualizar(
                nombre,
                apellido,
                correo,
                telefono
        );

        return clienteRepository.save(clienteActualizado);
    }

    @Override
    public Cliente actualizarEstadoCliente(UUID id, Boolean activo) {
        Cliente clienteExistente = obtenerClientePorId(id);

        Cliente clienteActualizado = clienteExistente.cambiarEstado(activo);

        return clienteRepository.save(clienteActualizado);
    }
    
}
