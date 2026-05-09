package com.authapp.application.rbac;

import com.authapp.domain.model.Usuario;
import com.authapp.domain.port.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioAdminService {

    private final UsuarioRepositoryPort usuarioPort;
    private final PasswordEncoder passwordEncoder;

    public Usuario crearUsuario(String email, String password, boolean activo) {
        if (usuarioPort.existsByEmail(email))
            throw new IllegalArgumentException("El email ya está registrado");

        if (password == null || password.length() < 8)
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");

        Usuario nuevo = new Usuario();
        nuevo.setEmail(email);
        nuevo.setPasswordHash(passwordEncoder.encode(password));
        nuevo.setMfaEnabled(false);
        nuevo.setActivo(activo);

        return usuarioPort.save(nuevo);
    }

    public Usuario actualizarUsuario(Long id, String email, boolean activo, String password) {
        Usuario usuario = usuarioPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Si cambia el email, verificar que no exista
        if (!usuario.getEmail().equals(email) && usuarioPort.existsByEmail(email))
            throw new IllegalArgumentException("El email ya está en uso");

        usuario.setEmail(email);
        usuario.setActivo(activo);

        // Solo actualizar password si se envió uno nuevo
        if (password != null && !password.isBlank()) {
            if (password.length() < 8)
                throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
            usuario.setPasswordHash(passwordEncoder.encode(password));
        }

        return usuarioPort.save(usuario);
    }
}
