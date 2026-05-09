import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../core/services/admin.service';
import { Rol, Permiso } from '../../../core/models/models';

@Component({
  selector: 'app-roles',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page-header flex-between">
      <div>
        <h1>Roles</h1>
        <p>RBAC — Gestión de roles y sus permisos asignados</p>
      </div>
      <button class="btn btn--primary" (click)="openModal()">+ Nuevo rol</button>
    </div>

    @if (error()) { <div class="alert alert--error">{{ error() }}</div> }

    <div class="card">
      <table class="data-table">
        <thead><tr><th>#</th><th>Nombre</th><th>Descripción</th><th>Permisos</th><th>Acciones</th></tr></thead>
        <tbody>
          @for (r of roles(); track r.id) {
            <tr>
              <td class="td-primary">{{ r.id }}</td>
              <td class="td-primary">{{ r.nombre }}</td>
              <td>{{ r.descripcion || '—' }}</td>
              <td>
                <div style="display:flex;gap:4px;flex-wrap:wrap">
                  @for (p of r.permisos; track p.id) {
                    <span class="badge badge--green" style="font-size:9px">{{ p.nombre }}</span>
                  } @empty { <span class="badge badge--gray">Sin permisos</span> }
                </div>
              </td>
              <td style="display:flex;gap:6px">
                <button class="btn btn--ghost btn--sm" (click)="openPermisosModal(r)">Permisos</button>
                <button class="btn btn--ghost btn--sm" (click)="openModal(r)">Editar</button>
                <button class="btn btn--danger btn--sm" (click)="onDelete(r)">Eliminar</button>
              </td>
            </tr>
          } @empty {
            <tr><td colspan="5" style="text-align:center;color:var(--text-dim);padding:32px">Sin roles</td></tr>
          }
        </tbody>
      </table>
    </div>

    @if (showModal()) {
      <div class="modal-overlay" (click)="closeModal()">
        <div class="modal" (click)="$event.stopPropagation()">
          <h3>{{ editingRol() ? 'Editar rol' : 'Nuevo rol' }}</h3>
          @if (modalError()) { <div class="alert alert--error">{{ modalError() }}</div> }
          <div class="form-group"><label>Nombre</label><input type="text" [(ngModel)]="formNombre" placeholder="ADMIN, EDITOR..." /></div>
          <div class="form-group"><label>Descripción</label><input type="text" [(ngModel)]="formDesc" placeholder="Descripción opcional" /></div>
          <div class="modal-actions">
            <button class="btn btn--ghost" (click)="closeModal()">Cancelar</button>
            <button class="btn btn--primary" (click)="onSave()" [disabled]="saving()">
              @if (saving()) { <span class="spinner"></span> } {{ editingRol() ? 'Actualizar' : 'Crear' }}
            </button>
          </div>
        </div>
      </div>
    }

    @if (showPermisosModal()) {
      <div class="modal-overlay" (click)="closePermisosModal()">
        <div class="modal" style="width:520px" (click)="$event.stopPropagation()">
          <h3>Permisos de "{{ selectedRol()?.nombre }}"</h3>
          <p style="font-size:12px;color:var(--text-muted);margin-bottom:20px">Selecciona los permisos para este rol.</p>
          @if (modalError()) { <div class="alert alert--error">{{ modalError() }}</div> }
          <div style="display:flex;flex-direction:column;gap:10px">
            @for (p of todosPermisos(); track p.id) {
              <label style="display:flex;align-items:center;gap:10px;cursor:pointer;padding:10px;border:1px solid var(--border);border-radius:6px"
                     [style.border-color]="isPermisoSelected(p.id) ? 'var(--accent-dim)' : 'var(--border)'"
                     [style.background]="isPermisoSelected(p.id) ? 'var(--accent-glow)' : 'transparent'">
                <input type="checkbox" [checked]="isPermisoSelected(p.id)" (change)="togglePermiso(p.id)"
                       style="width:14px;height:14px;accent-color:var(--accent)" />
                <div>
                  <div style="font-size:12px;font-weight:600;color:var(--text)">{{ p.nombre }}</div>
                  <div style="font-size:11px;color:var(--text-muted)">{{ p.descripcion }}</div>
                </div>
              </label>
            }
          </div>
          <div class="modal-actions">
            <button class="btn btn--ghost" (click)="closePermisosModal()">Cerrar</button>
            <button class="btn btn--primary" (click)="onGuardarPermisos()" [disabled]="saving()">
              @if (saving()) { <span class="spinner"></span> } Guardar permisos
            </button>
          </div>
        </div>
      </div>
    }
  `
})
export class RolesComponent implements OnInit {
  roles             = signal<Rol[]>([]);
  todosPermisos     = signal<Permiso[]>([]);
  error             = signal('');
  modalError        = signal('');
  showModal         = signal(false);
  showPermisosModal = signal(false);
  editingRol        = signal<Rol | null>(null);
  selectedRol       = signal<Rol | null>(null);
  saving            = signal(false);
  selectedPermisoIds = new Set<number>();
  formNombre = '';
  formDesc   = '';

  constructor(private adminSvc: AdminService) {}

  ngOnInit(): void {
    this.load();
    this.adminSvc.listarPermisos().subscribe((p: Permiso[]) => this.todosPermisos.set(p));
  }

  load(): void {
    this.adminSvc.listarRoles().subscribe({
      next: (r: Rol[]) => this.roles.set(r),
      error: () => this.error.set('Error cargando roles')
    });
  }

  openModal(r?: Rol): void {
    this.modalError.set('');
    this.editingRol.set(r ?? null);
    this.formNombre = r?.nombre ?? '';
    this.formDesc   = r?.descripcion ?? '';
    this.showModal.set(true);
  }

  closeModal(): void { this.showModal.set(false); }

  onSave(): void {
    if (!this.formNombre) { this.modalError.set('El nombre es obligatorio'); return; }
    this.saving.set(true);
    const op = this.editingRol()
      ? this.adminSvc.actualizarRol(this.editingRol()!.id, this.formNombre, this.formDesc)
      : this.adminSvc.crearRol(this.formNombre, this.formDesc);
    op.subscribe({
      next: () => { this.saving.set(false); this.closeModal(); this.load(); },
      error: (e: { error?: { message?: string } }) => { this.saving.set(false); this.modalError.set(e.error?.message || 'Error'); }
    });
  }

  onDelete(r: Rol): void {
    if (!confirm(`¿Eliminar rol "${r.nombre}"?`)) return;
    this.adminSvc.eliminarRol(r.id).subscribe({ next: () => this.load() });
  }

  openPermisosModal(r: Rol): void {
    this.modalError.set('');
    this.selectedRol.set(r);
    this.selectedPermisoIds = new Set(r.permisos.map((p: Permiso) => p.id));
    this.showPermisosModal.set(true);
  }

  closePermisosModal(): void { this.showPermisosModal.set(false); }

  isPermisoSelected(id: number): boolean { return this.selectedPermisoIds.has(id); }

  togglePermiso(id: number): void {
    if (this.selectedPermisoIds.has(id)) this.selectedPermisoIds.delete(id);
    else this.selectedPermisoIds.add(id);
  }

  onGuardarPermisos(): void {
    const rol = this.selectedRol();
    if (!rol) return;
    this.saving.set(true);
    this.adminSvc.asignarPermisosARol(rol.id, Array.from(this.selectedPermisoIds)).subscribe({
      next: () => { this.saving.set(false); this.closePermisosModal(); this.load(); },
      error: (e: { error?: { message?: string } }) => { this.saving.set(false); this.modalError.set(e.error?.message || 'Error'); }
    });
  }
}
