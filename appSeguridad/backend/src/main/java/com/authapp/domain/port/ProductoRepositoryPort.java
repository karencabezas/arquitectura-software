package com.authapp.domain.port;

import com.authapp.domain.model.Producto;
import java.util.List;
import java.util.Optional;

/**
 * PORT de salida definido por el dominio para productos (recurso ABAC).
 */
public interface ProductoRepositoryPort {
    List<Producto> findAllActivos();
    Optional<Producto> findById(Long id);
    Producto save(Producto producto);
}
