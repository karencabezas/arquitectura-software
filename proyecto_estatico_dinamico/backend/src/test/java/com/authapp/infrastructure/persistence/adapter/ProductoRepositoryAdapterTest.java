package com.authapp.infrastructure.persistence.adapter;

import com.authapp.domain.model.Producto;
import com.authapp.infrastructure.persistence.entity.ProductoEntity;
import com.authapp.infrastructure.persistence.repository.ProductoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductoRepositoryAdapter")
class ProductoRepositoryAdapterTest {

    @Mock ProductoRepository jpaRepository;
    @InjectMocks ProductoRepositoryAdapter adapter;

    private ProductoEntity entityBase(Long id) {
        ProductoEntity e = new ProductoEntity();
        e.setId(id);
        e.setNombre("Laptop");
        e.setDescripcion("Desc");
        e.setPrecio(new BigDecimal("999.99"));
        e.setCategoria("Tech");
        e.setOwnerId(1L);
        e.setActivo(true);
        return e;
    }

    @Test
    @DisplayName("findAllActivos() retorna solo los productos activos mapeados")
    void findAllActivos() {
        when(jpaRepository.findByActivoTrue()).thenReturn(List.of(entityBase(1L), entityBase(2L)));

        List<Producto> result = adapter.findAllActivos();

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(Producto::isActivo);
    }

    @Test
    @DisplayName("findAllActivos() retorna lista vacía cuando no hay activos")
    void findAllActivosVacio() {
        when(jpaRepository.findByActivoTrue()).thenReturn(List.of());

        assertThat(adapter.findAllActivos()).isEmpty();
    }

    @Test
    @DisplayName("findById() retorna el producto cuando existe")
    void findByIdExiste() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(entityBase(1L)));

        Optional<Producto> result = adapter.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getNombre()).isEqualTo("Laptop");
    }

    @Test
    @DisplayName("findById() retorna empty cuando no existe")
    void findByIdNoExiste() {
        when(jpaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(adapter.findById(99L)).isEmpty();
    }

    @Test
    @DisplayName("save() persiste y retorna el producto mapeado al dominio")
    void save() {
        ProductoEntity saved = entityBase(10L);
        when(jpaRepository.save(any())).thenReturn(saved);

        Producto input = new Producto(null, "Monitor", "HD", new BigDecimal("300"), "Tech", 1L, true);
        Producto result = adapter.save(input);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getNombre()).isEqualTo("Laptop"); // viene del entity guardado
        verify(jpaRepository).save(any());
    }
}
