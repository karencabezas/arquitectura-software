package com.authapp.infrastructure.persistence.adapter;

import com.authapp.domain.model.Permiso;
import com.authapp.infrastructure.persistence.entity.PermisoEntity;
import com.authapp.infrastructure.persistence.repository.PermisoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PermisoRepositoryAdapter")
class PermisoRepositoryAdapterTest {

    @Mock PermisoRepository jpaRepository;
    @InjectMocks PermisoRepositoryAdapter adapter;

    private PermisoEntity entityBase(Long id, String nombre) {
        return new PermisoEntity(id, nombre, "Descripción de " + nombre);
    }

    @Test
    @DisplayName("findAll() retorna todos los permisos mapeados al dominio")
    void findAll() {
        when(jpaRepository.findAll()).thenReturn(List.of(
                entityBase(1L, "PRODUCT_SELECT"),
                entityBase(2L, "PRODUCT_INSERT"),
                entityBase(3L, "PRODUCT_DELETE")));

        List<Permiso> result = adapter.findAll();

        assertThat(result).hasSize(3);
        assertThat(result).extracting(Permiso::getNombre)
                .containsExactlyInAnyOrder("PRODUCT_SELECT", "PRODUCT_INSERT", "PRODUCT_DELETE");
    }

    @Test
    @DisplayName("findAll() retorna lista vacía cuando no hay permisos")
    void findAllVacio() {
        when(jpaRepository.findAll()).thenReturn(List.of());

        assertThat(adapter.findAll()).isEmpty();
    }

    @Test
    @DisplayName("findById() retorna el permiso cuando existe")
    void findByIdExiste() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(entityBase(1L, "PRODUCT_SELECT")));

        Optional<Permiso> result = adapter.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getNombre()).isEqualTo("PRODUCT_SELECT");
    }

    @Test
    @DisplayName("findById() retorna empty cuando no existe")
    void findByIdNoExiste() {
        when(jpaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(adapter.findById(99L)).isEmpty();
    }

    @Test
    @DisplayName("findAllByIds() retorna los permisos correspondientes a los ids dados")
    void findAllByIds() {
        List<Long> ids = List.of(1L, 2L);
        when(jpaRepository.findAllById(ids)).thenReturn(List.of(
                entityBase(1L, "PRODUCT_SELECT"),
                entityBase(2L, "PRODUCT_INSERT")));

        List<Permiso> result = adapter.findAllByIds(ids);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Permiso::getNombre)
                .containsExactlyInAnyOrder("PRODUCT_SELECT", "PRODUCT_INSERT");
    }

    @Test
    @DisplayName("findAllByIds() retorna lista vacía cuando ningún id coincide")
    void findAllByIdsVacio() {
        when(jpaRepository.findAllById(List.of(99L))).thenReturn(List.of());

        assertThat(adapter.findAllByIds(List.of(99L))).isEmpty();
    }

    @Test
    @DisplayName("findAllByIds() con lista vacía retorna lista vacía")
    void findAllByIdsListaVacia() {
        when(jpaRepository.findAllById(List.of())).thenReturn(List.of());

        assertThat(adapter.findAllByIds(List.of())).isEmpty();
    }
}
