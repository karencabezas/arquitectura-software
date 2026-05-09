import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, UsuarioCreateRequest, UsuarioUpdateRequest } from '../../../core/services/admin.service';
import { Usuario, Rol } from '../../../core/models/models';

type ModalMode = 'create' | 'edit' | 'roles' | null;

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page-header flex-between">
      <div>
        <h1>Usuarios</h1>
        <p>Gestión de usuarios y asignación de roles</p>
      </div>
    </div>

    @if (error()) { <div class="alert alert--error">{{ error() }}</div> }

    <div class="card">
      <table class="data-table">
        <thead>
          <tr>
            <th>#</th>
            <th>Email</th>
            <th>MFA</th>
            <th>Estado</th>
            <th>Roles</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          @for (u of usuarios(); track u.id) {
            <tr>
              <td class="td-primary">{{ u.id }}</td>
              <td class="td-primary">{{ u.email }}</td>
              <td>
                <span [class]="u.mfaEnabled ? 'badge badge--green' : 'badge badge--warn'">
                  {{ u.mfaEnabled ? 'Activo' : 'Sin MFA' }}
                </span>
              </td>
              <td>
                <span [class]="u.activo ? 'badge badge--green' : 'badge badge--red'">
                  {{ u.activo ? 'Activo' : 'Inactivo' }}
                </span>
              </td>
              <td>
                <div style="display:flex;gap:4px;flex-wrap:wrap">
                  @for (r of u.roles; track r.id) {
                    <span class="badge badge--gray" style="font-size:9px">{{ r.nombre }}</span>
                  } @empty {
                    <span style="color:var(--text-dim);font-size:11px">Sin roles</span>
                  }
                </div>
              </td>
              <td style="display:flex;gap:6px">
                <button class="btn btn--ghost btn--sm" (click)="openEdit(u)">Editar</button>
                <button class="btn btn--ghost btn--sm" (click)="openRoles(u)">Roles</button>
              </td>
            </tr>
          } @empty {
            <tr>
              <td colspan="6" style="text-align:center;color:var(--text-dim);padding:32px">
                Sin usuarios registrados
              </td>
            </tr>
          }
        </tbody>
      </table>
    </div>

    <!-- ── Modal Crear Usuario ── -->
    @if (modalMode() === 'create') {
      <div class="modal-overlay" (click)="closeModal()">
        <div class="modal" (click)="$event.stopPropagation()">
          <p style="font-size:12px;color:var(--text-muted);margin-bottom:20px">
            El usuario deberá configurar su MFA en el primer inicio de sesión.
          </p>

          @if (modalError()) { <div class="alert alert--error">{{ modalError() }}</div> }

          <div class="form-group">
            <label>Email</label>
            <input type="email" [(ngModel)]="createForm.email"
                   placeholder="usuario@ejemplo.com" />
          </div>

          <div class="form-group">
            <label>Contraseña</label>
            <input type="password" [(ngModel)]="createForm.password"
                   placeholder="Mínimo 8 caracteres" />
          </div>

          <div class="form-group">
            <label>Confirmar contraseña</label>
            <input type="password" [(ngModel)]="confirmPassword"
                   placeholder="Repite la contraseña" />
          </div>

          <div class="form-group">
            <label>Estado</label>
            <select [(ngModel)]="createForm.activo">
              <option [ngValue]="true">Activo</option>
              <option [ngValue]="false">Inactivo</option>
            </select>
          </div>

          <div class="modal-actions">
            <button class="btn btn--ghost" (click)="closeModal()">Cancelar</button>
            <button class="btn btn--primary" (click)="onCreate()" [disabled]="saving()">
              @if (saving()) { <span class="spinner"></span> } Crear usuario
            </button>
          </div>
        </div>
      </div>
    }

    <!-- ── Modal Editar Usuario ── -->
    @if (modalMode() === 'edit') {
      <div class="modal-overlay" (click)="closeModal()">
        <div class="modal" (click)="$event.stopPropagation()">
          <h3>Editar usuario</h3>

          @if (modalError()) { <div class="alert alert--error">{{ modalError() }}</div> }

          <div class="form-group">
            <label>Email</label>
            <input type="email" [(ngModel)]="editForm.email"
                   placeholder="usuario@ejemplo.com" />
          </div>

          <div class="form-group">
            <label>Nueva contraseña
              <span style="color:var(--text-dim);font-weight:400;font-size:10px">
                (dejar vacío para no cambiar)
              </span>
            </label>
            <input type="password" [(ngModel)]="editForm.password"
                   placeholder="Solo si deseas cambiarla" />
          </div>

          <div class="form-group">
            <label>Estado</label>
            <select [(ngModel)]="editForm.activo">
              <option [ngValue]="true">Activo</option>
              <option [ngValue]="false">Inactivo</option>
            </select>
          </div>

          <div class="form-group">
            <label>MFA</label>
            <div style="padding:10px 14px;background:var(--bg-input);border:1px solid var(--border);border-radius:var(--radius)">
              <span [class]="selectedUser()?.mfaEnabled ? 'badge badge--green' : 'badge badge--warn'">
                {{ selectedUser()?.mfaEnabled ? 'Configurado' : 'No configurado' }}
              </span>
              <span style="font-size:11px;color:var(--text-muted);margin-left:8px">
                El usuario lo configura en su primer login
              </span>
            </div>
          </div>

          <div class="modal-actions">
            <button class="btn btn--ghost" (click)="closeModal()">Cancelar</button>
            <button class="btn btn--primary" (click)="onEdit()" [disabled]="saving()">
              @if (saving()) { <span class="spinner"></span> } Guardar cambios
            </button>
          </div>
        </div>
      </div>
    }

    <!-- ── Modal Roles ── -->
    @if (modalMode() === 'roles') {
      <div class="modal-overlay" (click)="closeModal()">
        <div class="modal" style="width:480px" (click)="$event.stopPropagation()">
          <h3>Roles de "{{ selectedUser()?.email }}"</h3>
          <p style="font-size:12px;color:var(--text-muted);margin-bottom:20px">
            Los cambios se aplican inmediatamente.
          </p>
          @if (modalError()) { <div class="alert alert--error">{{ modalError() }}</div> }

          <div style="display:flex;flex-direction:column;gap:10px">
            @for (r of todosRoles(); track r.id) {
              <label style="display:flex;align-items:center;gap:10px;cursor:pointer;padding:10px;
                            border:1px solid var(--border);border-radius:6px;transition:var(--transition)"
                     [style.border-color]="isRolSelected(r.id) ? 'var(--accent-dim)' : 'var(--border)'"
                     [style.background]="isRolSelected(r.id) ? 'var(--accent-glow)' : 'transparent'">
                <input type="checkbox" [checked]="isRolSelected(r.id)" (change)="toggleRol(r)"
                       style="width:14px;height:14px;accent-color:var(--accent)" />
                <div>
                  <div style="font-size:12px;font-weight:600;color:var(--text)">{{ r.nombre }}</div>
                  <div style="font-size:11px;color:var(--text-muted)">{{ r.descripcion }}</div>
                </div>
              </label>
            }
          </div>

          <div class="modal-actions">
            <button class="btn btn--ghost" (click)="closeModal()">Cerrar</button>
          </div>
        </div>
      </div>
    }
  `
})
export class UsersComponent implements OnInit {
  usuarios     = signal<Usuario[]>([]);
  todosRoles   = signal<Rol[]>([]);
  error        = signal('');
  modalError   = signal('');
  modalMode    = signal<ModalMode>(null);
  selectedUser = signal<Usuario | null>(null);
  saving       = signal(false);
  selectedRolIds = new Set<number>();
  confirmPassword = '';

  createForm: UsuarioCreateRequest = { email: '', password: '', activo: true };
  editForm: UsuarioUpdateRequest & { password?: string } = { email: '', activo: true, password: '' };

  constructor(private adminSvc: AdminService) {}

  ngOnInit(): void {
    this.load();
    this.adminSvc.listarRoles().subscribe((r: Rol[]) => this.todosRoles.set(r));
  }

  load(): void {
    this.adminSvc.listarUsuarios().subscribe({
      next: (u: Usuario[]) => this.usuarios.set(u),
      error: () => this.error.set('Error cargando usuarios')
    });
  }

  // ── Abrir modales ──

  openCreate(): void {
    this.modalError.set('');
    this.createForm = { email: '', password: '', activo: true };
    this.confirmPassword = '';
    this.modalMode.set('create');
  }

  openEdit(u: Usuario): void {
    this.modalError.set('');
    this.selectedUser.set(u);
    this.editForm = { email: u.email, activo: u.activo, password: '' };
    this.modalMode.set('edit');
  }

  openRoles(u: Usuario): void {
    this.modalError.set('');
    this.selectedUser.set(u);
    this.selectedRolIds = new Set(u.roles.map((r: Rol) => r.id));
    this.modalMode.set('roles');
  }

  closeModal(): void {
    this.modalMode.set(null);
    this.load();
  }

  // ── Acciones ──

  onCreate(): void {
    this.modalError.set('');

    if (!this.createForm.email || !this.createForm.password) {
      this.modalError.set('Email y contraseña son obligatorios');
      return;
    }
    if (this.createForm.password.length < 8) {
      this.modalError.set('La contraseña debe tener al menos 8 caracteres');
      return;
    }
    if (this.createForm.password !== this.confirmPassword) {
      this.modalError.set('Las contraseñas no coinciden');
      return;
    }

    this.saving.set(true);
    this.adminSvc.crearUsuario(this.createForm).subscribe({
      next: () => { this.saving.set(false); this.closeModal(); },
      error: (e: { error?: { message?: string } }) => {
        this.saving.set(false);
        this.modalError.set(e.error?.message || 'Error al crear usuario');
      }
    });
  }

  onEdit(): void {
    this.modalError.set('');
    const u = this.selectedUser();
    if (!u) return;

    if (!this.editForm.email) {
      this.modalError.set('El email es obligatorio');
      return;
    }
    if (this.editForm.password && this.editForm.password.length < 8) {
      this.modalError.set('La contraseña debe tener al menos 8 caracteres');
      return;
    }

    this.saving.set(true);
    this.adminSvc.actualizarUsuario(u.id, this.editForm).subscribe({
      next: () => { this.saving.set(false); this.closeModal(); },
      error: (e: { error?: { message?: string } }) => {
        this.saving.set(false);
        this.modalError.set(e.error?.message || 'Error al actualizar usuario');
      }
    });
  }

  // ── Roles ──

  isRolSelected(id: number): boolean { return this.selectedRolIds.has(id); }

  toggleRol(rol: Rol): void {
    const u = this.selectedUser();
    if (!u) return;
    if (this.selectedRolIds.has(rol.id)) {
      this.adminSvc.removerRolDeUsuario(u.id, rol.id).subscribe({
        next: () => this.selectedRolIds.delete(rol.id),
        error: (e: { error?: { message?: string } }) => this.modalError.set(e.error?.message || 'Error al remover rol')
      });
    } else {
      this.adminSvc.asignarRolAUsuario(u.id, rol.id).subscribe({
        next: () => this.selectedRolIds.add(rol.id),
        error: (e: { error?: { message?: string } }) => this.modalError.set(e.error?.message || 'Error al asignar rol')
      });
    }
  }
}
