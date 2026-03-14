package com.example.productosapi.domain.service;

import com.example.productosapi.domain.model.Cliente;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 *
 * @author kcabezas
 */
public interface ClienteService {
    
    Cliente crearCliente(Cliente cliente);
    
    Cliente obtenerClientePorId(UUID id);
    
    Cliente obtenerClientePorNumeroDocumento(String numeroDocumento);
    
    List<Cliente> obtenerTodosLosClientes();
    
    void eliminarCliente(UUID id);
    
    Cliente actualizarCliente(UUID id, String nombre, String apellido,String correo,String telefono);
    
    Cliente actualizarEstadoCliente(UUID id, Boolean activo);

}
