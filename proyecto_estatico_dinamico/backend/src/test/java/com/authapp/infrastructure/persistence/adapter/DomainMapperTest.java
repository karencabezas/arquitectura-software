package com.authapp.infrastructure.persistence.adapter;

import com.authapp.domain.model.Permiso;
import com.authapp.domain.model.Producto;
import com.authapp.domain.model.Rol;
import com.authapp.domain.model.Usuario;
import com.authapp.infrastructure.persistence.entity.PermisoEntity;
import com.authapp.infrastructure.persistence.entity.ProductoEntity;
import com.authapp.infrastructure.persistence.entity.RolEntity;
import com.authapp.infrastructure.persistence.entity.UsuarioEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DomainMapper")
class DomainMapperTest {

    // ══════════════════════════════════════════════════════
    // PERMISO
    // ══════════════════════════════════════════════════════

    @Nested
    @DisplayName("Permiso")
    class PermisoMapping {

        @Test
        @DisplayName("toDomain(PermisoEntity) mapea todos los campos correctamente")
        void toDomain() {
            PermisoEntity e = new PermisoEntity(1L, "PRODUCT_SELECT", "Ver productos");

            Permiso p = DomainMapper.toDomain(e);

            assertThat(p.getId()).isEqualTo(1L);
            assertThat(p.getNombre()).isEqualTo("PRODUCT_SELECT");
            assertThat(p.getDescripcion()).isEqualTo("Ver productos");
        }

        @Test
        @DisplayName("toDomain(null) retorna null")
        void toDomainNull() {
            assertThat(DomainMapper.toDomain((PermisoEntity) null)).isNull();
        }

        @Test
        @DisplayName("toEntity(Permiso) mapea todos los campos correctamente")
        void toEntity() {
            Permiso p = new Permiso(2L, "PRODUCT_DELETE", "Eliminar productos");

            PermisoEntity e = DomainMapper.toEntity(p);

            assertThat(e.getId()).isEqualTo(2L);
            assertThat(e.getNombre()).isEqualTo("PRODUCT_DELETE");
            assertThat(e.getDescripcion()).isEqualTo("Eliminar productos");
        }

        @Test
        @DisplayName("toEntity(null) retorna null")
        void toEntityNull() {
            assertThat(DomainMapper.toEntity((Permiso) null)).isNull();
        }
    }

    // ══════════════════════════════════════════════════════
    // ROL
    // ══════════════════════════════════════════════════════

    @Nested
    @DisplayName("Rol")
    class RolMapping {

        private RolEntity rolEntityConPermisos() {
            PermisoEntity pe = new PermisoEntity(1L, "PRODUCT_SELECT", "Ver");
            RolEntity re = new RolEntity();
            re.setId(10L);
            re.setNombre("USER");
            re.setDescripcion("Usuario normal");
            re.setPermisos(Set.of(pe));
            return re;
        }

        @Test
        @DisplayName("toDomain(RolEntity) mapea id, nombre, descripción y permisos")
        void toDomain() {
            Rol rol = DomainMapper.toDomain(rolEntityConPermisos());

            assertThat(rol.getId()).isEqualTo(10L);
            assertThat(rol.getNombre()).isEqualTo("USER");
            assertThat(rol.getDescripcion()).isEqualTo("Usuario normal");
            assertThat(rol.getPermisos()).hasSize(1);
            assertThat(rol.getPermisos().iterator().next().getNombre()).isEqualTo("PRODUCT_SELECT");
        }

        @Test
        @DisplayName("toDomain(null) retorna null")
        void toDomainNull() {
            assertThat(DomainMapper.toDomain((RolEntity) null)).isNull();
        }

        @Test
        @DisplayName("toEntity(Rol) mapea id, nombre, descripción y permisos")
        void toEntity() {
            Permiso p = new Permiso(1L, "PRODUCT_INSERT", "Crear");
            Rol rol = new Rol(5L, "ADMIN", "Administrador", Set.of(p));

            RolEntity entity = DomainMapper.toEntity(rol);

            assertThat(entity.getId()).isEqualTo(5L);
            assertThat(entity.getNombre()).isEqualTo("ADMIN");
            assertThat(entity.getDescripcion()).isEqualTo("Administrador");
            assertThat(entity.getPermisos()).hasSize(1);
        }

        @Test
        @DisplayName("toEntity(null) retorna null")
        void toEntityNull() {
            assertThat(DomainMapper.toEntity((Rol) null)).isNull();
        }
    }

    // ══════════════════════════════════════════════════════
    // USUARIO
    // ══════════════════════════════════════════════════════

    @Nested
    @DisplayName("Usuario")
    class UsuarioMapping {

        private UsuarioEntity usuarioEntity() {
            PermisoEntity pe = new PermisoEntity(1L, "PRODUCT_SELECT", "Ver");
            RolEntity re = new RolEntity();
            re.setId(1L);
            re.setNombre("USER");
            re.setDescripcion("desc");
            re.setPermisos(Set.of(pe));

            UsuarioEntity ue = new UsuarioEntity();
            ue.setId(99L);
            ue.setEmail("karen@test.com");
            ue.setPasswordHash("hashed");
            ue.setTotpSecret("SECRET");
            ue.setMfaEnabled(true);
            ue.setActivo(true);
            ue.setRoles(Set.of(re));
            return ue;
        }

        @Test
        @DisplayName("toDomain(UsuarioEntity) mapea todos los campos incluyendo roles")
        void toDomain() {
            Usuario u = DomainMapper.toDomain(usuarioEntity());

            assertThat(u.getId()).isEqualTo(99L);
            assertThat(u.getEmail()).isEqualTo("karen@test.com");
            assertThat(u.getPasswordHash()).isEqualTo("hashed");
            assertThat(u.getTotpSecret()).isEqualTo("SECRET");
            assertThat(u.isMfaEnabled()).isTrue();
            assertThat(u.isActivo()).isTrue();
            assertThat(u.getRoles()).hasSize(1);
        }

        @Test
        @DisplayName("toDomain(null) retorna null")
        void toDomainNull() {
            assertThat(DomainMapper.toDomain((UsuarioEntity) null)).isNull();
        }

        @Test
        @DisplayName("toEntity(Usuario) mapea todos los campos incluyendo roles")
        void toEntity() {
            Permiso p = new Permiso(1L, "PRODUCT_SELECT", "Ver");
            Rol rol = new Rol(1L, "USER", "desc", Set.of(p));
            Usuario u = new Usuario(7L, "karen@test.com", "hash", "SEC", true, true, Set.of(rol));

            UsuarioEntity entity = DomainMapper.toEntity(u);

            assertThat(entity.getId()).isEqualTo(7L);
            assertThat(entity.getEmail()).isEqualTo("karen@test.com");
            assertThat(entity.getPasswordHash()).isEqualTo("hash");
            assertThat(entity.getTotpSecret()).isEqualTo("SEC");
            assertThat(entity.isMfaEnabled()).isTrue();
            assertThat(entity.isActivo()).isTrue();
            assertThat(entity.getRoles()).hasSize(1);
        }

        @Test
        @DisplayName("toEntity(null) retorna null")
        void toEntityNull() {
            assertThat(DomainMapper.toEntity((Usuario) null)).isNull();
        }
    }

    // ══════════════════════════════════════════════════════
    // PRODUCTO
    // ══════════════════════════════════════════════════════

    @Nested
    @DisplayName("Producto")
    class ProductoMapping {

        @Test
        @DisplayName("toDomain(ProductoEntity) mapea todos los campos correctamente")
        void toDomain() {
            ProductoEntity e = new ProductoEntity();
            e.setId(3L);
            e.setNombre("Laptop");
            e.setDescripcion("Desc");
            e.setPrecio(new BigDecimal("999.99"));
            e.setCategoria("Tech");
            e.setOwnerId(1L);
            e.setActivo(true);

            Producto p = DomainMapper.toDomain(e);

            assertThat(p.getId()).isEqualTo(3L);
            assertThat(p.getNombre()).isEqualTo("Laptop");
            assertThat(p.getDescripcion()).isEqualTo("Desc");
            assertThat(p.getPrecio()).isEqualByComparingTo("999.99");
            assertThat(p.getCategoria()).isEqualTo("Tech");
            assertThat(p.getOwnerId()).isEqualTo(1L);
            assertThat(p.isActivo()).isTrue();
        }

        @Test
        @DisplayName("toDomain(null) retorna null")
        void toDomainNull() {
            assertThat(DomainMapper.toDomain((ProductoEntity) null)).isNull();
        }

        @Test
        @DisplayName("toEntity(Producto) mapea todos los campos correctamente")
        void toEntity() {
            Producto p = new Producto(4L, "Monitor", "HD", new BigDecimal("300.00"), "Tech", 2L, true);

            ProductoEntity e = DomainMapper.toEntity(p);

            assertThat(e.getId()).isEqualTo(4L);
            assertThat(e.getNombre()).isEqualTo("Monitor");
            assertThat(e.getDescripcion()).isEqualTo("HD");
            assertThat(e.getPrecio()).isEqualByComparingTo("300.00");
            assertThat(e.getCategoria()).isEqualTo("Tech");
            assertThat(e.getOwnerId()).isEqualTo(2L);
            assertThat(e.isActivo()).isTrue();
        }

        @Test
        @DisplayName("toEntity(null) retorna null")
        void toEntityNull() {
            assertThat(DomainMapper.toEntity((Producto) null)).isNull();
        }
    }
}
