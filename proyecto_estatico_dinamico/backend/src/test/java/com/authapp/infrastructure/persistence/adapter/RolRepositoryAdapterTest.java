package com.authapp.infrastructure.persistence.adapter;

import com.authapp.domain.model.Rol;
import com.authapp.infrastructure.persistence.entity.PermisoEntity;
import com.authapp.infrastructure.persistence.entity.RolEntity;
import com.authapp.infrastructure.persistence.repository.RolRepository;
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
@DisplayName("RolRepositoryAdapter")
class RolRepositoryAdapterTest {

    @Mock RolRepository jpaRepository;
    @InjectMocks RolRepositoryAdapter adapter;

    private RolEntity entityBase(Long id, String nombre) {
        PermisoEntity pe = new PermisoEntity(1L, "PRODUCT_SELECT", "Ver");
        RolEntity re = new RolEntity();
        re.setId(id);
        re.setNombre(nombre);
        re.setDescripcion("Descripción de " + nombre);
        re.setPermisos(Set.of(pe));
        return re;
    }

    @Test
    @DisplayName("findAll() retorna todos los roles mapeados al dominio")
    void findAll() {
        when(jpaRepository.findAll()).thenReturn(
                List.of(entityBase(1L, "ADMIN"), entityBase(2L, "USER")));

        List<Rol> result = adapter.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Rol::getNombre).containsExactlyInAnyOrder("ADMIN", "USER");
    }

    @Test
    @DisplayName("findAll() retorna lista vacía cuando no hay roles")
    void findAllVacio() {
        when(jpaRepository.findAll()).thenReturn(List.of());

        assertThat(adapter.findAll()).isEmpty();
    }

    @Test
    @DisplayName("findById() retorna el rol cuando existe")
    void findByIdExiste() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(entityBase(1L, "ADMIN")));

        Optional<Rol> result = adapter.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getNombre()).isEqualTo("ADMIN");
        assertThat(result.get().getPermisos()).hasSize(1);
    }

    @Test
    @DisplayName("findById() retorna empty cuando no existe")
    void findByIdNoExiste() {
        when(jpaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(adapter.findById(99L)).isEmpty();
    }

    @Test
    @DisplayName("findByNombre() retorna el rol cuando existe")
    void findByNombreExiste() {
        when(jpaRepository.findByNombre("ADMIN")).thenReturn(Optional.of(entityBase(1L, "ADMIN")));

        Optional<Rol> result = adapter.findByNombre("ADMIN");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findByNombre() retorna empty cuando no existe")
    void findByNombreNoExiste() {
        when(jpaRepository.findByNombre("INEXISTENTE")).thenReturn(Optional.empty());

        assertThat(adapter.findByNombre("INEXISTENTE")).isEmpty();
    }

    @Test
    @DisplayName("existsByNombre() retorna true cuando existe")
    void existsByNombreTrue() {
        when(jpaRepository.existsByNombre("ADMIN")).thenReturn(true);

        assertThat(adapter.existsByNombre("ADMIN")).isTrue();
    }

    @Test
    @DisplayName("existsByNombre() retorna false cuando no existe")
    void existsByNombreFalse() {
        when(jpaRepository.existsByNombre("EDITOR")).thenReturn(false);

        assertThat(adapter.existsByNombre("EDITOR")).isFalse();
    }

    @Test
    @DisplayName("save() persiste el rol y retorna el dominio mapeado")
    void save() {
        RolEntity saved = entityBase(5L, "EDITOR");
        when(jpaRepository.save(any())).thenReturn(saved);

        Rol input = new Rol(null, "EDITOR", "Editor", new HashSet<>());
        Rol result = adapter.save(input);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getNombre()).isEqualTo("EDITOR");
        verify(jpaRepository).save(any());
    }

    @Test
    @DisplayName("deleteById() delega la eliminación al repositorio JPA")
    void deleteById() {
        doNothing().when(jpaRepository).deleteById(1L);

        adapter.deleteById(1L);

        verify(jpaRepository).deleteById(1L);
    }
}
