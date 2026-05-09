package com.authapp.infrastructure.persistence.adapter;

import com.authapp.domain.model.Usuario;
import com.authapp.infrastructure.persistence.entity.PermisoEntity;
import com.authapp.infrastructure.persistence.entity.RolEntity;
import com.authapp.infrastructure.persistence.entity.UsuarioEntity;
import com.authapp.infrastructure.persistence.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioRepositoryAdapter")
class UsuarioRepositoryAdapterTest {

    @Mock UsuarioRepository jpaRepository;
    @InjectMocks UsuarioRepositoryAdapter adapter;

    private UsuarioEntity entityBase() {
        PermisoEntity pe = new PermisoEntity(1L, "PRODUCT_SELECT", "Ver");
        RolEntity re = new RolEntity();
        re.setId(1L);
        re.setNombre("USER");
        re.setDescripcion("desc");
        re.setPermisos(Set.of(pe));

        UsuarioEntity ue = new UsuarioEntity();
        ue.setId(1L);
        ue.setEmail("karen@test.com");
        ue.setPasswordHash("hash");
        ue.setTotpSecret("SEC");
        ue.setMfaEnabled(false);
        ue.setActivo(true);
        ue.setRoles(Set.of(re));
        return ue;
    }

    @Test
    @DisplayName("findByEmail() retorna el usuario cuando existe")
    void findByEmailExiste() {
        when(jpaRepository.findByEmail("karen@test.com")).thenReturn(Optional.of(entityBase()));

        Optional<Usuario> result = adapter.findByEmail("karen@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("karen@test.com");
        assertThat(result.get().getRoles()).hasSize(1);
    }

    @Test
    @DisplayName("findByEmail() retorna empty cuando no existe")
    void findByEmailNoExiste() {
        when(jpaRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        Optional<Usuario> result = adapter.findByEmail("noexiste@test.com");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findById() retorna el usuario cuando existe")
    void findByIdExiste() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(entityBase()));

        Optional<Usuario> result = adapter.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findById() retorna empty cuando no existe")
    void findByIdNoExiste() {
        when(jpaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Usuario> result = adapter.findById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAll() retorna la lista completa mapeada a dominio")
    void findAll() {
        when(jpaRepository.findAll()).thenReturn(List.of(entityBase(), entityBase()));

        List<Usuario> result = adapter.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getEmail()).isEqualTo("karen@test.com");
    }

    @Test
    @DisplayName("findAll() retorna lista vacía cuando no hay usuarios")
    void findAllVacio() {
        when(jpaRepository.findAll()).thenReturn(List.of());

        List<Usuario> result = adapter.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("existsByEmail() retorna true cuando existe")
    void existsByEmailTrue() {
        when(jpaRepository.existsByEmail("karen@test.com")).thenReturn(true);

        assertThat(adapter.existsByEmail("karen@test.com")).isTrue();
    }

    @Test
    @DisplayName("existsByEmail() retorna false cuando no existe")
    void existsByEmailFalse() {
        when(jpaRepository.existsByEmail("nuevo@test.com")).thenReturn(false);

        assertThat(adapter.existsByEmail("nuevo@test.com")).isFalse();
    }

    @Test
    @DisplayName("save() persiste la entidad y retorna el dominio mapeado")
    void save() {
        UsuarioEntity saved = entityBase();
        saved.setId(42L);
        when(jpaRepository.save(any())).thenReturn(saved);

        Usuario input = new Usuario(null, "karen@test.com", "hash", "SEC", false, true, new HashSet<>());
        Usuario result = adapter.save(input);

        assertThat(result.getId()).isEqualTo(42L);
        assertThat(result.getEmail()).isEqualTo("karen@test.com");
        verify(jpaRepository).save(any());
    }
}
