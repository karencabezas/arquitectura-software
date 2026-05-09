package com.authapp.application.product;

import com.authapp.domain.model.Producto;
import com.authapp.domain.port.ProductoRepositoryPort;
import com.authapp.infrastructure.security.AppUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductoService")
class ProductoServiceTest {

    @Mock ProductoRepositoryPort productoPort;

    @InjectMocks ProductoService productoService;

    // ── Helpers ──────────────────────────────────────────────────────────

    private Producto producto(Long id, Long ownerId) {
        return new Producto(id, "Laptop", "Descripcion", BigDecimal.valueOf(999), "Tech", ownerId, true);
    }

    private AppUserPrincipal principalAdmin() {
        return new AppUserPrincipal(1L, "admin@test.com",
                List.of("ADMIN"),
                List.of("PRODUCT_SELECT", "PRODUCT_INSERT", "PRODUCT_UPDATE", "PRODUCT_DELETE"));
    }

    private AppUserPrincipal principalUser(Long id) {
        return new AppUserPrincipal(id, "user@test.com",
                List.of("USER"),
                List.of("PRODUCT_SELECT", "PRODUCT_INSERT", "PRODUCT_UPDATE", "PRODUCT_DELETE"));
    }

    private AppUserPrincipal principalSinPermisos(Long id) {
        return new AppUserPrincipal(id, "limitado@test.com",
                List.of("USER"),
                List.of("PRODUCT_SELECT"));
    }

    // ═══════════════════════════════════════════════════════════════════
    // LISTAR
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("listar() retorna todos los productos activos")
    void listar() {
        List<Producto> lista = List.of(producto(1L, 10L), producto(2L, 20L));
        when(productoPort.findAllActivos()).thenReturn(lista);

        List<Producto> result = productoService.listar();

        assertThat(result).hasSize(2);
    }

    // ═══════════════════════════════════════════════════════════════════
    // BUSCAR POR ID
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("buscarPorId() retorna el producto cuando existe")
    void buscarPorIdExiste() {
        Producto p = producto(1L, 10L);
        when(productoPort.findById(1L)).thenReturn(Optional.of(p));

        Producto result = productoService.buscarPorId(1L);

        assertThat(result.getNombre()).isEqualTo("Laptop");
    }

    @Test
    @DisplayName("buscarPorId() lanza excepción cuando no existe")
    void buscarPorIdNoExiste() {
        when(productoPort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.buscarPorId(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Producto no encontrado");
    }

    // ═══════════════════════════════════════════════════════════════════
    // CREAR
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("crear()")
    class Crear {

        @Test
        @DisplayName("ADMIN puede crear producto")
        void adminPuedeCrear() {
            when(productoPort.save(any())).thenAnswer(i -> i.getArgument(0));

            Producto result = productoService.crear("Teclado", "Mec", BigDecimal.TEN, "Tech", principalAdmin());

            assertThat(result.getNombre()).isEqualTo("Teclado");
            assertThat(result.getOwnerId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("USER con permiso PRODUCT_INSERT puede crear producto")
        void userConPermisoInsertPuedeCrear() {
            when(productoPort.save(any())).thenAnswer(i -> i.getArgument(0));

            Producto result = productoService.crear("Mouse", "Inalambrico", BigDecimal.TEN, "Tech", principalUser(2L));

            assertThat(result.getOwnerId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("USER sin permiso PRODUCT_INSERT lanza AccessDeniedException")
        void userSinPermisoNoPuedeCrear() {
            assertThatThrownBy(() ->
                productoService.crear("X", "Y", BigDecimal.ONE, "Z", principalSinPermisos(2L))
            ).isInstanceOf(AccessDeniedException.class)
             .hasMessageContaining("permiso para crear");
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // ACTUALIZAR
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("actualizar()")
    class Actualizar {

        @Test
        @DisplayName("ADMIN puede actualizar cualquier producto")
        void adminPuedeActualizar() {
            Producto p = producto(1L, 99L); // dueño es 99, admin es 1
            when(productoPort.findById(1L)).thenReturn(Optional.of(p));
            when(productoPort.save(any())).thenAnswer(i -> i.getArgument(0));

            Producto result = productoService.actualizar(1L, "Nuevo nombre", "Desc", BigDecimal.TEN, "Cat", principalAdmin());

            assertThat(result.getNombre()).isEqualTo("Nuevo nombre");
        }

        @Test
        @DisplayName("dueño con permiso puede actualizar su propio producto")
        void duenoPuedeActualizar() {
            Producto p = producto(1L, 2L); // dueño es 2
            when(productoPort.findById(1L)).thenReturn(Optional.of(p));
            when(productoPort.save(any())).thenAnswer(i -> i.getArgument(0));

            Producto result = productoService.actualizar(1L, "Actualizado", "Desc", BigDecimal.TEN, "Cat", principalUser(2L));

            assertThat(result.getNombre()).isEqualTo("Actualizado");
        }

        @Test
        @DisplayName("usuario que NO es dueño lanza AccessDeniedException")
        void noduenioNoPuedeActualizar() {
            Producto p = producto(1L, 99L); // dueño es 99
            when(productoPort.findById(1L)).thenReturn(Optional.of(p));

            assertThatThrownBy(() ->
                productoService.actualizar(1L, "X", "Y", BigDecimal.ONE, "Z", principalUser(2L))
            ).isInstanceOf(AccessDeniedException.class)
             .hasMessageContaining("dueño");
        }

        @Test
        @DisplayName("usuario sin permiso PRODUCT_UPDATE lanza AccessDeniedException")
        void sinPermisoUpdateNoPuedeActualizar() {
            Producto p = producto(1L, 2L); // dueño es 2
            when(productoPort.findById(1L)).thenReturn(Optional.of(p));

            assertThatThrownBy(() ->
                productoService.actualizar(1L, "X", "Y", BigDecimal.ONE, "Z", principalSinPermisos(2L))
            ).isInstanceOf(AccessDeniedException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // ELIMINAR
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("eliminar()")
    class Eliminar {

        @Test
        @DisplayName("ADMIN puede eliminar cualquier producto (soft delete)")
        void adminPuedeEliminar() {
            Producto p = producto(1L, 99L);
            when(productoPort.findById(1L)).thenReturn(Optional.of(p));
            when(productoPort.save(any())).thenAnswer(i -> i.getArgument(0));

            productoService.eliminar(1L, principalAdmin());

            verify(productoPort).save(argThat(saved -> !saved.isActivo()));
        }

        @Test
        @DisplayName("dueño con permiso PRODUCT_DELETE puede eliminar su producto")
        void duenoPuedeEliminar() {
            Producto p = producto(1L, 2L);
            when(productoPort.findById(1L)).thenReturn(Optional.of(p));
            when(productoPort.save(any())).thenAnswer(i -> i.getArgument(0));

            productoService.eliminar(1L, principalUser(2L));

            verify(productoPort).save(argThat(saved -> !saved.isActivo()));
        }

        @Test
        @DisplayName("usuario que NO es dueño lanza AccessDeniedException")
        void noDuenioNoPuedeEliminar() {
            Producto p = producto(1L, 99L);
            when(productoPort.findById(1L)).thenReturn(Optional.of(p));

            assertThatThrownBy(() -> productoService.eliminar(1L, principalUser(2L)))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("dueño");
        }

        @Test
        @DisplayName("usuario sin permiso PRODUCT_DELETE lanza AccessDeniedException")
        void sinPermisoDeleteNoPuedeEliminar() {
            Producto p = producto(1L, 2L); // dueño es 2
            when(productoPort.findById(1L)).thenReturn(Optional.of(p));

            assertThatThrownBy(() -> productoService.eliminar(1L, principalSinPermisos(2L)))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }
}
