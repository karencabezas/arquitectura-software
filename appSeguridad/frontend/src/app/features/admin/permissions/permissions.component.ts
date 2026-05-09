import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService } from '../../../core/services/admin.service';
import { Permiso } from '../../../core/models/models';

@Component({
  selector: 'app-permissions',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page-header">
      <h1>Permisos</h1>
      <p>Permisos disponibles en el sistema para asignar a roles</p>
    </div>

    @if (error()) { <div class="alert alert--error">{{ error() }}</div> }

    <div class="card">
      <table class="data-table">
        <thead><tr><th>#</th><th>Nombre</th><th>Descripción</th><th>Contexto</th></tr></thead>
        <tbody>
          @for (p of permisos(); track p.id) {
            <tr>
              <td class="td-primary">{{ p.id }}</td>
              <td><span class="badge badge--green" style="font-size:10px;letter-spacing:0.5px">{{ p.nombre }}</span></td>
              <td>{{ p.descripcion }}</td>
              <td><span class="badge badge--warn">PRODUCT</span></td>
            </tr>
          } @empty {
            <tr><td colspan="4" style="text-align:center;color:var(--text-dim);padding:32px">Sin permisos</td></tr>
          }
        </tbody>
      </table>
    </div>


  `
})
export class PermissionsComponent implements OnInit {
  permisos = signal<Permiso[]>([]);
  error    = signal('');

  constructor(private adminSvc: AdminService) {}

  ngOnInit(): void {
    this.adminSvc.listarPermisos().subscribe({
      next: (p: Permiso[]) => this.permisos.set(p),
      error: () => this.error.set('Error cargando permisos')
    });
  }
}
