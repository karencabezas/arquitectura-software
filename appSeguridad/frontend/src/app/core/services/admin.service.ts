import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Rol, Permiso, Usuario, Producto, ProductoRequest } from '../models/models';

export interface UsuarioCreateRequest {
  email: string;
  password: string;
  activo: boolean;
}

export interface UsuarioUpdateRequest {
  email: string;
  activo: boolean;
  password?: string;
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly API = 'http://localhost:8080/api/admin';

  constructor(private http: HttpClient) {}

  // ── Roles ──
  listarRoles(): Observable<Rol[]> {
    return this.http.get<Rol[]>(`${this.API}/roles`);
  }
  crearRol(nombre: string, descripcion: string): Observable<Rol> {
    return this.http.post<Rol>(`${this.API}/roles`, { nombre, descripcion });
  }
  actualizarRol(id: number, nombre: string, descripcion: string): Observable<Rol> {
    return this.http.put<Rol>(`${this.API}/roles/${id}`, { nombre, descripcion });
  }
  eliminarRol(id: number): Observable<unknown> {
    return this.http.delete(`${this.API}/roles/${id}`);
  }

  // ── Permisos ──
  listarPermisos(): Observable<Permiso[]> {
    return this.http.get<Permiso[]>(`${this.API}/permisos`);
  }
  asignarPermisosARol(rolId: number, permisoIds: number[]): Observable<unknown> {
    return this.http.post(`${this.API}/roles/${rolId}/permisos`, { permisoIds });
  }
  removerPermisoDeRol(rolId: number, permisoId: number): Observable<unknown> {
    return this.http.delete(`${this.API}/roles/${rolId}/permisos/${permisoId}`);
  }

  // ── Usuarios ──
  listarUsuarios(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(`${this.API}/usuarios`);
  }
  crearUsuario(req: UsuarioCreateRequest): Observable<Usuario> {
    return this.http.post<Usuario>(`${this.API}/usuarios`, req);
  }
  actualizarUsuario(id: number, req: UsuarioUpdateRequest): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.API}/usuarios/${id}`, req);
  }
  asignarRolAUsuario(usuarioId: number, rolId: number): Observable<unknown> {
    return this.http.post(`${this.API}/usuarios/${usuarioId}/roles/${rolId}`, {});
  }
  removerRolDeUsuario(usuarioId: number, rolId: number): Observable<unknown> {
    return this.http.delete(`${this.API}/usuarios/${usuarioId}/roles/${rolId}`);
  }
}

@Injectable({ providedIn: 'root' })
export class ProductoService {
  private readonly API = 'http://localhost:8080/api/productos';

  constructor(private http: HttpClient) {}

  listar(): Observable<Producto[]> {
    return this.http.get<Producto[]>(this.API);
  }
  crear(req: ProductoRequest): Observable<Producto> {
    return this.http.post<Producto>(this.API, req);
  }
  actualizar(id: number, req: ProductoRequest): Observable<Producto> {
    return this.http.put<Producto>(`${this.API}/${id}`, req);
  }
  eliminar(id: number): Observable<unknown> {
    return this.http.delete(`${this.API}/${id}`);
  }
}
