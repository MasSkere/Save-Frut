import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../core/services/admin.service';
import { AuthService, mensajeError } from '../../core/services/auth.service';
import { UsuarioAdmin } from '../../shared/models/panel.model';

@Component({
  selector: 'app-admin-usuarios',
  standalone: true,
  imports: [FormsModule, DatePipe],
  template: `
    <div class="flex flex-wrap items-center justify-between gap-3 mb-6">
      <h1 class="text-2xl font-bold text-gray-900">Usuarios</h1>
      <label class="text-sm flex items-center gap-2">Rol
        <select class="input-sf !w-auto" [ngModel]="tipo()" (ngModelChange)="filtrar($event)">
          <option value="">Todos</option>
          <option value="CLIENTE">Clientes</option>
          <option value="VENDEDOR">Vendedores</option>
          <option value="ADMIN">Administradores</option>
        </select>
      </label>
    </div>
    @if (error()) {
      <div class="mb-4 bg-red-50 border border-red-200 rounded-card p-3 text-sm text-red-700" role="alert">{{ error() }}</div>
    }
    <div class="card-sf overflow-x-auto">
      <table class="tabla-sf">
        <thead><tr><th>#</th><th>Email</th><th>Rol</th><th>Registro</th><th>Estado</th><th></th></tr></thead>
        <tbody>
          @for (u of usuarios(); track u.id) {
            <tr>
              <td>{{ u.id }}</td>
              <td>{{ u.email }}</td>
              <td>{{ u.tipo.replace('ROLE_', '') }}</td>
              <td>{{ u.fechaRegistro | date: 'dd/MM/yyyy' }}</td>
              <td>
                <span class="text-xs font-semibold px-2 py-1 rounded-full" [class]="u.estado === 'BLOQUEADO' ? 'bg-red-100 text-red-700' : 'bg-brand-100 text-brand-700'">{{ u.estado }}</span>
              </td>
              <td>
                @if (u.id !== auth.user()?.id) {
                  <button type="button" class="text-xs hover:underline" [class]="u.estado === 'BLOQUEADO' ? 'text-brand-600' : 'text-red-600'"
                          (click)="alternar(u)">{{ u.estado === 'BLOQUEADO' ? 'Desbloquear' : 'Bloquear' }}</button>
                }
              </td>
            </tr>
          }
        </tbody>
      </table>
    </div>
  `
})
export class AdminUsuariosComponent implements OnInit {
  private readonly service = inject(AdminService);
  readonly auth = inject(AuthService);
  readonly usuarios = signal<UsuarioAdmin[]>([]);
  readonly tipo = signal('');
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.cargar();
  }

  filtrar(tipo: string): void {
    this.tipo.set(tipo);
    this.cargar();
  }

  alternar(u: UsuarioAdmin): void {
    const estado = u.estado === 'BLOQUEADO' ? 'ACTIVO' : 'BLOQUEADO';
    this.service.cambiarEstadoUsuario(u.id, estado).subscribe({
      next: act => this.usuarios.update(l => l.map(x => x.id === act.id ? act : x)),
      error: err => this.error.set(mensajeError(err))
    });
  }

  private cargar(): void {
    this.service.getUsuarios(this.tipo() || undefined).subscribe({
      next: u => this.usuarios.set(u),
      error: err => this.error.set(mensajeError(err))
    });
  }
}
