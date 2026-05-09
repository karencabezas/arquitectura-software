package com.authapp.infrastructure.web.controller;

import com.authapp.application.product.ProductoService;
import com.authapp.infrastructure.security.AppUserPrincipal;
import com.authapp.infrastructure.web.dto.ProductoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    public ResponseEntity<?> listar() {
        return ResponseEntity.ok(productoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody ProductoRequest req,
                                   @AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(
            productoService.crear(req.getNombre(), req.getDescripcion(),
                                  req.getPrecio(), req.getCategoria(), principal)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @RequestBody ProductoRequest req,
                                        @AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(
            productoService.actualizar(id, req.getNombre(), req.getDescripcion(),
                                       req.getPrecio(), req.getCategoria(), principal)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id,
                                      @AuthenticationPrincipal AppUserPrincipal principal) {
        productoService.eliminar(id, principal);
        return ResponseEntity.ok(Map.of("message", "Producto eliminado"));
    }
}
