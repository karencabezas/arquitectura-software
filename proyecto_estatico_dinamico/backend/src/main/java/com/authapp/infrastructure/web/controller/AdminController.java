package com.authapp.infrastructure.web.controller;

import com.authapp.application.rbac.RbacService;
import com.authapp.application.rbac.UsuarioAdminService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final RbacService rbacService;
    private final UsuarioAdminService usuarioAdminService;

    // ── Roles ──

    @GetMapping("/roles")
    public ResponseEntity<?> listarRoles() {
        return ResponseEntity.ok(rbacService.listarRoles());
    }

    @PostMapping("/roles")
    public ResponseEntity<?> crearRol(@RequestBody RolRequest req) {
        return ResponseEntity.ok(rbacService.crearRol(req.nombre, req.descripcion));
    }

    @PutMapping("/roles/{id}")
    public ResponseEntity<?> actualizarRol(@PathVariable Long id, @RequestBody RolRequest req) {
        return ResponseEntity.ok(rbacService.actualizarRol(id, req.nombre, req.descripcion));
    }

    @DeleteMapping("/roles/{id}")
    public ResponseEntity<?> eliminarRol(@PathVariable Long id) {
        rbacService.eliminarRol(id);
        return ResponseEntity.ok(Map.of("message", "Rol eliminado"));
    }

    // ── Permisos ──

    @GetMapping("/permisos")
    public ResponseEntity<?> listarPermisos() {
        return ResponseEntity.ok(rbacService.listarPermisos());
    }

    @PostMapping("/roles/{rolId}/permisos")
    public ResponseEntity<?> asignarPermisos(@PathVariable Long rolId,
                                             @RequestBody PermisosRequest req) {
        rbacService.asignarPermisosARol(rolId, req.permisoIds);
        return ResponseEntity.ok(Map.of("message", "Permisos asignados"));
    }

    @DeleteMapping("/roles/{rolId}/permisos/{permisoId}")
    public ResponseEntity<?> removerPermiso(@PathVariable Long rolId,
                                            @PathVariable Long permisoId) {
        rbacService.removerPermisoDeRol(rolId, permisoId);
        return ResponseEntity.ok(Map.of("message", "Permiso removido"));
    }

    // ── Usuarios ──

    @GetMapping("/usuarios")
    public ResponseEntity<?> listarUsuarios() {
        return ResponseEntity.ok(rbacService.listarUsuarios());
    }

    @PostMapping("/usuarios")
    public ResponseEntity<?> crearUsuario(@RequestBody UsuarioCreateRequest req) {
        return ResponseEntity.ok(
            usuarioAdminService.crearUsuario(req.email, req.password, req.activo)
        );
    }

    @PutMapping("/usuarios/{id}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable Long id,
                                               @RequestBody UsuarioUpdateRequest req) {
        return ResponseEntity.ok(
            usuarioAdminService.actualizarUsuario(id, req.email, req.activo, req.password)
        );
    }

    @PostMapping("/usuarios/{usuarioId}/roles/{rolId}")
    public ResponseEntity<?> asignarRol(@PathVariable Long usuarioId,
                                        @PathVariable Long rolId) {
        rbacService.asignarRolAUsuario(usuarioId, rolId);
        return ResponseEntity.ok(Map.of("message", "Rol asignado"));
    }

    @DeleteMapping("/usuarios/{usuarioId}/roles/{rolId}")
    public ResponseEntity<?> removerRol(@PathVariable Long usuarioId,
                                        @PathVariable Long rolId) {
        rbacService.removerRolDeUsuario(usuarioId, rolId);
        return ResponseEntity.ok(Map.of("message", "Rol removido"));
    }

    // ── Request bodies ──

    @Data static class RolRequest {
        public String nombre;
        public String descripcion;
    }

    @Data static class PermisosRequest {
        public List<Long> permisoIds;
    }

    @Data static class UsuarioCreateRequest {
        public String email;
        public String password;
        public boolean activo = true;
    }

    @Data static class UsuarioUpdateRequest {
        public String email;
        public boolean activo;
        public String password; // opcional — si viene vacío no se cambia
    }
}
