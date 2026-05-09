package com.authapp.domain.port;

import com.authapp.domain.model.Permiso;
import java.util.List;
import java.util.Optional;

/**
 * PORT de salida definido por el dominio para permisos ABAC.
 */
public interface PermisoRepositoryPort {
    List<Permiso> findAll();
    Optional<Permiso> findById(Long id);
    List<Permiso> findAllByIds(List<Long> ids);
}
