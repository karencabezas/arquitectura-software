package com.authapp.infrastructure.persistence.adapter;

import com.authapp.domain.model.Permiso;
import com.authapp.domain.model.Producto;
import com.authapp.domain.model.Rol;
import com.authapp.domain.model.Usuario;
import com.authapp.infrastructure.persistence.entity.PermisoEntity;
import com.authapp.infrastructure.persistence.entity.ProductoEntity;
import com.authapp.infrastructure.persistence.entity.RolEntity;
import com.authapp.infrastructure.persistence.entity.UsuarioEntity;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper entre modelos de dominio y entidades JPA.
 *
 * Este mapper vive en infrastructure porque conoce AMBAS representaciones.
 * El dominio no sabe que existen las entidades JPA.
 * La infraestructura traduce entre los dos mundos.
 */
public class DomainMapper {

    private DomainMapper() {}

    // ── Permiso ──

    public static Permiso toDomain(PermisoEntity e) {
        if (e == null) return null;
        return new Permiso(e.getId(), e.getNombre(), e.getDescripcion());
    }

    public static PermisoEntity toEntity(Permiso d) {
        if (d == null) return null;
        PermisoEntity e = new PermisoEntity();
        e.setId(d.getId());
        e.setNombre(d.getNombre());
        e.setDescripcion(d.getDescripcion());
        return e;
    }

    // ── Rol ──

    public static Rol toDomain(RolEntity e) {
        if (e == null) return null;
        Set<Permiso> permisos = e.getPermisos().stream()
                .map(DomainMapper::toDomain)
                .collect(Collectors.toSet());
        return new Rol(e.getId(), e.getNombre(), e.getDescripcion(), permisos);
    }

    public static RolEntity toEntity(Rol d) {
        if (d == null) return null;
        RolEntity e = new RolEntity();
        e.setId(d.getId());
        e.setNombre(d.getNombre());
        e.setDescripcion(d.getDescripcion());
        Set<PermisoEntity> permisos = d.getPermisos().stream()
                .map(DomainMapper::toEntity)
                .collect(Collectors.toSet());
        e.setPermisos(permisos);
        return e;
    }

    // ── Usuario ──

    public static Usuario toDomain(UsuarioEntity e) {
        if (e == null) return null;
        Set<Rol> roles = e.getRoles().stream()
                .map(DomainMapper::toDomain)
                .collect(Collectors.toSet());
        return new Usuario(
                e.getId(), e.getEmail(), e.getPasswordHash(),
                e.getTotpSecret(), e.isMfaEnabled(), e.isActivo(), roles
        );
    }

    public static UsuarioEntity toEntity(Usuario d) {
        if (d == null) return null;
        UsuarioEntity e = new UsuarioEntity();
        e.setId(d.getId());
        e.setEmail(d.getEmail());
        e.setPasswordHash(d.getPasswordHash());
        e.setTotpSecret(d.getTotpSecret());
        e.setMfaEnabled(d.isMfaEnabled());
        e.setActivo(d.isActivo());
        Set<RolEntity> roles = d.getRoles().stream()
                .map(DomainMapper::toEntity)
                .collect(Collectors.toSet());
        e.setRoles(roles);
        return e;
    }

    // ── Producto ──

    public static Producto toDomain(ProductoEntity e) {
        if (e == null) return null;
        return new Producto(
                e.getId(), e.getNombre(), e.getDescripcion(),
                e.getPrecio(), e.getCategoria(), e.getOwnerId(), e.isActivo()
        );
    }

    public static ProductoEntity toEntity(Producto d) {
        if (d == null) return null;
        ProductoEntity e = new ProductoEntity();
        e.setId(d.getId());
        e.setNombre(d.getNombre());
        e.setDescripcion(d.getDescripcion());
        e.setPrecio(d.getPrecio());
        e.setCategoria(d.getCategoria());
        e.setOwnerId(d.getOwnerId());
        e.setActivo(d.isActivo());
        return e;
    }
}
