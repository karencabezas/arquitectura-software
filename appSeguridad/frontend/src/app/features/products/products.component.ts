import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { ProductoService } from '../../core/services/admin.service';
import { Producto, ProductoRequest } from '../../core/models/models';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page-header flex-between">
      <div>
        <h1>Productos</h1>
        <p>Gestión de productos</p>
      </div>
      @if (canInsert()) {
        <button class="btn btn--primary" (click)="openModal()">+ Nuevo producto</button>
      }
    </div>

    @if (error()) { <div class="alert alert--error">{{ error() }}</div> }

    @if (loading()) {
      <div style="color:var(--text-muted);font-size:12px;padding:40px 0;text-align:center">
        <span class="spinner" style="border-color:rgba(0,229,160,0.3);border-top-color:var(--accent);width:20px;height:20px;margin-bottom:10px;display:block;margin-inline:auto"></span>
        Cargando productos...
      </div>
    } @else {
      <div class="card">
        <table class="data-table">
          <thead>
            <tr><th>#</th><th>Nombre</th><th>Categoría</th><th>Precio</th><th>Dueño</th><th>Estado</th><th>Acciones</th></tr>
          </thead>
          <tbody>
            @for (p of productos(); track p.id) {
              <tr>
                <td class="td-primary">{{ p.id }}</td>
                <td class="td-primary">{{ p.nombre }}</td>
                <td><span class="badge badge--gray">{{ p.categoria || '—' }}</span></td>
                <td class="text-accent">S/ {{ p.precio | number:'1.2-2' }}</td>
                <td style="font-size:11px">{{ p.ownerId === currentUserId() ? '⬡ Tú' : 'ID ' + p.ownerId }}</td>
                <td><span [class]="p.activo ? 'badge badge--green' : 'badge badge--red'">{{ p.activo ? 'Activo' : 'Inactivo' }}</span></td>
                <td style="display:flex;gap:6px">
                  @if (canUpdate(p)) { <button class="btn btn--ghost btn--sm" (click)="openModal(p)">Editar</button> }
                  @if (canDelete(p)) { <button class="btn btn--danger btn--sm" (click)="onDelete(p)">Eliminar</button> }
                  @if (!canUpdate(p) && !canDelete(p)) { <span style="color:var(--text-dim);font-size:11px">Solo lectura</span> }
                </td>
              </tr>
            } @empty {
              <tr><td colspan="7" style="text-align:center;color:var(--text-dim);padding:32px">No hay productos registrados</td></tr>
            }
          </tbody>
        </table>
      </div>
    }

    <div class="card" style="margin-top:20px;border-color:var(--border-hi)">
      <p style="font-size:11px;color:var(--text-muted);margin-bottom:10px;font-weight:600;letter-spacing:0.5px;text-transform:uppercase">Tus permisos activos</p>
      <div style="display:flex;gap:8px;flex-wrap:wrap">
        @for (p of abacPermisos; track p) {
          <span [class]="hasPermission(p) ? 'badge badge--green' : 'badge badge--red'">
            {{ hasPermission(p) ? '✓' : '✗' }} {{ p }}
          </span>
        }
      </div>
    </div>

    @if (showModal()) {
      <div class="modal-overlay" (click)="closeModal()">
        <div class="modal" (click)="$event.stopPropagation()">
          <h3>{{ editingId() ? 'Editar producto' : 'Nuevo producto' }}</h3>
          @if (modalError()) { <div class="alert alert--error">{{ modalError() }}</div> }
          <div class="form-group"><label>Nombre</label><input type="text" [(ngModel)]="form.nombre" placeholder="Nombre del producto" /></div>
          <div class="form-group"><label>Descripción</label><input type="text" [(ngModel)]="form.descripcion" placeholder="Descripción opcional" /></div>
          <div class="grid-2">
            <div class="form-group"><label>Precio (S/)</label><input type="number" [(ngModel)]="form.precio" placeholder="0.00" min="0" step="0.01" /></div>
            <div class="form-group"><label>Categoría</label><input type="text" [(ngModel)]="form.categoria" placeholder="Electrónica..." /></div>
          </div>
          <div class="modal-actions">
            <button class="btn btn--ghost" (click)="closeModal()">Cancelar</button>
            <button class="btn btn--primary" (click)="onSave()" [disabled]="saving()">
              @if (saving()) { <span class="spinner"></span> } {{ editingId() ? 'Actualizar' : 'Crear' }}
            </button>
          </div>
        </div>
      </div>
    }
  `
})
export class ProductsComponent implements OnInit {
  productos     = signal<Producto[]>([]);
  loading       = signal(true);
  error         = signal('');
  showModal     = signal(false);
  editingId     = signal<number | null>(null);
  saving        = signal(false);
  modalError    = signal('');
  currentUserId = signal<number>(0);
  abacPermisos  = ['PRODUCT_SELECT', 'PRODUCT_INSERT', 'PRODUCT_UPDATE', 'PRODUCT_DELETE'];

  form: ProductoRequest = { nombre: '', descripcion: '', precio: 0, categoria: '' };

  constructor(private productoSvc: ProductoService, private auth: AuthService) {}

  ngOnInit(): void {
    this.currentUserId.set(this.auth.currentUser()?.id ?? 0);
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.productoSvc.listar().subscribe({
      next: (data: Producto[]) => { this.productos.set(data); this.loading.set(false); },
      error: () => { this.error.set('Error cargando productos'); this.loading.set(false); }
    });
  }

  canInsert(): boolean { return this.auth.isAdmin() || this.auth.hasPermission('PRODUCT_INSERT'); }
  canUpdate(p: Producto): boolean { return this.auth.isAdmin() || (this.auth.hasPermission('PRODUCT_UPDATE') && p.ownerId === this.currentUserId()); }
  canDelete(p: Producto): boolean { return this.auth.isAdmin() || (this.auth.hasPermission('PRODUCT_DELETE') && p.ownerId === this.currentUserId()); }
  hasPermission(p: string): boolean { return this.auth.isAdmin() || this.auth.hasPermission(p); }

  openModal(p?: Producto): void {
    this.modalError.set('');
    if (p) {
      this.editingId.set(p.id);
      this.form = { nombre: p.nombre, descripcion: p.descripcion, precio: p.precio, categoria: p.categoria };
    } else {
      this.editingId.set(null);
      this.form = { nombre: '', descripcion: '', precio: 0, categoria: '' };
    }
    this.showModal.set(true);
  }

  closeModal(): void { this.showModal.set(false); }

  onSave(): void {
    if (!this.form.nombre || !this.form.precio) { this.modalError.set('Nombre y precio son obligatorios'); return; }
    this.saving.set(true);
    const op = this.editingId()
      ? this.productoSvc.actualizar(this.editingId()!, this.form)
      : this.productoSvc.crear(this.form);
    op.subscribe({
      next: () => { this.saving.set(false); this.closeModal(); this.load(); },
      error: (err: { error?: { message?: string } }) => { this.saving.set(false); this.modalError.set(err.error?.message || 'Error al guardar'); }
    });
  }

  onDelete(p: Producto): void {
    if (!confirm(`¿Eliminar "${p.nombre}"?`)) return;
    this.productoSvc.eliminar(p.id).subscribe({
      next: () => this.load(),
      error: (err: { error?: { message?: string } }) => this.error.set(err.error?.message || 'Error al eliminar')
    });
  }
}
