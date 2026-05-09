package com.authapp.domain.port;

import com.authapp.domain.model.Rol;
import java.util.List;
import java.util.Optional;

/**
 * PORT de salida definido por el dominio para la gestión de roles (RBAC).
 */
public interface RolRepositoryPort {
    List<Rol> findAll();
    Optional<Rol> findById(Long id);
    Optional<Rol> findByNombre(String nombre);
    boolean existsByNombre(String nombre);
    Rol save(Rol rol);
    void deleteById(Long id);
}
