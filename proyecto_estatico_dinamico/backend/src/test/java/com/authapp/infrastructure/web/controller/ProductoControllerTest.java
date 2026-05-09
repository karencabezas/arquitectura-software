package com.authapp.infrastructure.web.controller;

import com.authapp.application.product.ProductoService;
import com.authapp.domain.model.Producto;
import com.authapp.infrastructure.security.AppUserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductoController.class)
@Import({com.authapp.infrastructure.security.SecurityConfig.class,
         com.authapp.infrastructure.security.JwtAuthFilter.class,
         com.authapp.infrastructure.security.JwtService.class})
@DisplayName("ProductoController")
class ProductoControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean ProductoService productoService;

    private static final AppUserPrincipal PRINCIPAL =
            new AppUserPrincipal(1L, "karen@test.com", List.of("ADMIN"), List.of("PRODUCT_SELECT", "PRODUCT_INSERT", "PRODUCT_UPDATE", "PRODUCT_DELETE"));

    private Producto productoEjemplo() {
        return new Producto(1L, "Laptop", "Descripción", new BigDecimal("999.99"), "Tech", 1L, true);
    }

    @Nested
    @DisplayName("GET /api/productos")
    class Listar {

        @Test
        @DisplayName("retorna 200 con lista de productos")
        void listar() throws Exception {
            when(productoService.listar()).thenReturn(List.of(productoEjemplo()));

            mockMvc.perform(get("/api/productos")
                    .with(user("karen@test.com").roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].nombre").value("Laptop"));
        }
    }

    @Nested
    @DisplayName("GET /api/productos/{id}")
    class BuscarPorId {

        @Test
        @DisplayName("retorna 200 con el producto cuando existe")
        void buscarPorId() throws Exception {
            when(productoService.buscarPorId(1L)).thenReturn(productoEjemplo());

            mockMvc.perform(get("/api/productos/1")
                    .with(user("karen@test.com").roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @DisplayName("retorna 400 cuando el servicio lanza IllegalArgumentException")
        void productoNoEncontrado() throws Exception {
            when(productoService.buscarPorId(99L))
                    .thenThrow(new IllegalArgumentException("Producto no encontrado"));

            mockMvc.perform(get("/api/productos/99")
                    .with(user("karen@test.com").roles("ADMIN")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
        }
    }

    @Nested
    @DisplayName("POST /api/productos")
    class Crear {

        @Test
        @DisplayName("retorna 200 con el producto creado")
        void crear() throws Exception {
            when(productoService.crear(any(), any(), any(), any(), any()))
                    .thenReturn(productoEjemplo());

            String body = "{\"nombre\":\"Laptop\",\"descripcion\":\"Desc\",\"precio\":999.99,\"categoria\":\"Tech\"}";

            mockMvc.perform(post("/api/productos")
                    .with(csrf())
                    .with(user("karen@test.com").roles("ADMIN"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nombre").value("Laptop"));
        }
    }

    @Nested
    @DisplayName("PUT /api/productos/{id}")
    class Actualizar {

        @Test
        @DisplayName("retorna 200 con el producto actualizado")
        void actualizar() throws Exception {
            Producto actualizado = new Producto(1L, "Laptop Pro", "Nueva desc", new BigDecimal("1200.00"), "Tech", 1L, true);
            when(productoService.actualizar(anyLong(), any(), any(), any(), any(), any()))
                    .thenReturn(actualizado);

            String body = "{\"nombre\":\"Laptop Pro\",\"descripcion\":\"Nueva desc\",\"precio\":1200.00,\"categoria\":\"Tech\"}";

            mockMvc.perform(put("/api/productos/1")
                    .with(csrf())
                    .with(user("karen@test.com").roles("ADMIN"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nombre").value("Laptop Pro"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/productos/{id}")
    class Eliminar {

        @Test
        @DisplayName("retorna 200 con mensaje de confirmación")
        void eliminar() throws Exception {
            doNothing().when(productoService).eliminar(anyLong(), any());

            mockMvc.perform(delete("/api/productos/1")
                    .with(csrf())
                    .with(user("karen@test.com").roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Producto eliminado"));
        }

        @Test
        @DisplayName("retorna 403 cuando el servicio lanza AccessDeniedException")
        void eliminarSinPermiso() throws Exception {
            doThrow(new org.springframework.security.access.AccessDeniedException("No permitido"))
                    .when(productoService).eliminar(anyLong(), any());

            mockMvc.perform(delete("/api/productos/1")
                    .with(csrf())
                    .with(user("karen@test.com").roles("ADMIN")))
                    .andExpect(status().isForbidden());
        }
    }
}
