package com.authapp.application.rbac;

import com.authapp.domain.model.Permiso;
import com.authapp.domain.model.Rol;
import com.authapp.domain.model.Usuario;
import com.authapp.domain.port.PermisoRepositoryPort;
import com.authapp.domain.port.RolRepositoryPort;
import com.authapp.domain.port.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Caso de uso: RBAC — gestión de roles, permisos y asignación a usuarios.
 * Depende únicamente de ports del dominio, no de JPA.
 */
@Service
@RequiredArgsConstructor
public class RbacService {

    private final RolRepositoryPort rolPort;
    private final PermisoRepositoryPort permisoPort;
    private final UsuarioRepositoryPort usuarioPort;

    // ── Roles ──

    public List<Rol> listarRoles() {
        return rolPort.findAll();
    }

    public Rol crearRol(String nombre, String descripcion) {
        if (rolPort.existsByNombre(nombre))
            throw new IllegalArgumentException("Ya existe un rol con ese nombre");
        Rol rol = new Rol(null, nombre, descripcion, new HashSet<>());
        return rolPort.save(rol);
    }

    public Rol actualizarRol(Long id, String nombre, String descripcion) {
        Rol rol = rolPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));
        rol.setNombre(nombre);
        rol.setDescripcion(descripcion);
        return rolPort.save(rol);
    }

    public void eliminarRol(Long id) {
        rolPort.deleteById(id);
    }

    // ── Permisos ──

    public List<Permiso> listarPermisos() {
        return permisoPort.findAll();
    }

    public void asignarPermisosARol(Long rolId, List<Long> permisoIds) {
        Rol rol = rolPort.findById(rolId)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));
        List<Permiso> permisos = permisoPort.findAllByIds(permisoIds);
        rol.getPermisos().addAll(permisos);
        rolPort.save(rol);
    }

    public void removerPermisoDeRol(Long rolId, Long permisoId) {
        Rol rol = rolPort.findById(rolId)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));
        rol.getPermisos().removeIf(p -> p.getId().equals(permisoId));
        rolPort.save(rol);
    }

    // ── Usuarios ──

    public List<Usuario> listarUsuarios() {
        return usuarioPort.findAll();
    }

    public void asignarRolAUsuario(Long usuarioId, Long rolId) {
        Usuario usuario = usuarioPort.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        Rol rol = rolPort.findById(rolId)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));
        usuario.getRoles().add(rol);
        usuarioPort.save(usuario);
    }

    public void removerRolDeUsuario(Long usuarioId, Long rolId) {
        Usuario usuario = usuarioPort.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        usuario.getRoles().removeIf(r -> r.getId().equals(rolId));
        usuarioPort.save(usuario);
    }
}
