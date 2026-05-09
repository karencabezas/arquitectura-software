package com.authapp.application.product;

import com.authapp.domain.model.Producto;
import com.authapp.domain.port.ProductoRepositoryPort;
import com.authapp.infrastructure.security.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Caso de uso: CRUD de Productos con reglas ABAC.
 * Depende de ProductoRepositoryPort (domain.port), no de JPA.
 *
 * Reglas ABAC implementadas aquí (en application), no en el controller:
 *   - SELECT: cualquier usuario autenticado
 *   - INSERT: permiso PRODUCT_INSERT o ADMIN
 *   - UPDATE: (dueño + PRODUCT_UPDATE) o ADMIN
 *   - DELETE: (dueño + PRODUCT_DELETE) o ADMIN
 */
@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepositoryPort productoPort;

    public List<Producto> listar() {
        return productoPort.findAllActivos();
    }

    public Producto buscarPorId(Long id) {
        return productoPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
    }

    public Producto crear(String nombre, String descripcion, BigDecimal precio,
                          String categoria, AppUserPrincipal principal) {
        if (!principal.hasPermission("PRODUCT_INSERT") && !principal.isAdmin())
            throw new AccessDeniedException("No tienes permiso para crear productos");

        Producto p = new Producto(null, nombre, descripcion, precio, categoria, principal.getId(), true);
        return productoPort.save(p);
    }

    public Producto actualizar(Long id, String nombre, String descripcion,
                               BigDecimal precio, String categoria, AppUserPrincipal principal) {
        Producto producto = buscarPorId(id);

        // Regla ABAC: dueño o ADMIN
        if (!principal.isAdmin() && !producto.perteneceA(principal.getId()))
            throw new AccessDeniedException("Solo el dueño o un administrador puede modificar este producto");

        if (!principal.hasPermission("PRODUCT_UPDATE") && !principal.isAdmin())
            throw new AccessDeniedException("No tienes permiso para actualizar productos");

        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecio(precio);
        producto.setCategoria(categoria);
        return productoPort.save(producto);
    }

    public void eliminar(Long id, AppUserPrincipal principal) {
        Producto producto = buscarPorId(id);

        // Regla ABAC: dueño o ADMIN — usando método del modelo de dominio
        if (!principal.isAdmin() && !producto.perteneceA(principal.getId()))
            throw new AccessDeniedException("Solo el dueño o un administrador puede eliminar este producto");

        if (!principal.hasPermission("PRODUCT_DELETE") && !principal.isAdmin())
            throw new AccessDeniedException("No tienes permiso para eliminar productos");

        producto.setActivo(false);
        productoPort.save(producto);
    }
}
