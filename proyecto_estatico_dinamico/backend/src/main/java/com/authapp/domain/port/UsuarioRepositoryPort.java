package com.authapp.domain.port;

import com.authapp.domain.model.Usuario;
import java.util.List;
import java.util.Optional;

/**
 * PORT de salida (driven port) definido por el dominio.
 *
 * El dominio declara QUÉ necesita. La infraestructura decide CÓMO lo implementa.
 * La capa application depende de esta interfaz, nunca de UsuarioRepository (JPA).
 *
 * Flujo de dependencia correcto:
 *   application → domain.port ← infrastructure.persistence
 */
public interface UsuarioRepositoryPort {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findById(Long id);
    List<Usuario> findAll();
    boolean existsByEmail(String email);
    Usuario save(Usuario usuario);
}
