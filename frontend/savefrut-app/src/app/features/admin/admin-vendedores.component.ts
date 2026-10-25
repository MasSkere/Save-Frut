import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { AdminService } from '../../core/services/admin.service';
import { mensajeError } from '../../core/services/auth.service';
import { VendedorAdmin } from '../../shared/models/panel.model';

@Component({
  selector: 'app-admin-vendedores',
  standalone: true,
  template: `
    <h1 class="text-2xl font-bold text-gray-900 mb-1">Vendedores</h1>
    <p class="text-sm text-gray-500 mb-6">{{ pendientes() }} solicitudes pendientes de aprobación. Solo las tiendas aprobadas aparecen en el directorio público.</p>
    @if (error()) {
      <div class="mb-4 bg-red-50 border border-red-200 rounded-card p-3 text-sm text-red-700" role="alert">{{ error() }}</div>
    }
    <div class="card-sf overflow-x-auto">
      <table class="tabla-sf">
        <thead><tr><th>Tienda</th><th>RUC</th><th>Email</th><th>Distrito</th><th>Estado</th><th></th></tr></thead>
        <tbody>
          @for (v of vendedores(); track v.id) {
            <tr>
              <td class="font-medium">{{ v.razonSocial }}</td>
              <td class="tabular-nums">{{ v.ruc }}</td>
              <td>{{ v.email }}</td>
              <td>{{ v.nombreDistrito ?? '—' }}</td>
              <td>
                <span class="text-xs font-semibold px-2 py-1 rounded-full" [class]="v.aprobado ? 'bg-brand-100 text-brand-700' : 'bg-amber-100 text-amber-800'">
                  {{ v.aprobado ? 'Aprobado' : 'Pendiente' }}
                </span>
              </td>
              <td>
                @if (v.aprobado) {
                  <button type="button" class="text-red-600 text-xs hover:underline" (click)="cambiar(v, false)">Suspender</button>
                } @else {
                  <button type="button" class="btn-primary !py-1 !px-3 !text-xs" (click)="cambiar(v, true)">Aprobar</button>
                }
              </td>
            </tr>
          }
        </tbody>
      </table>
    </div>
  `
})
export class AdminVendedoresComponent implements OnInit {
  private readonly service = inject(AdminService);
  readonly vendedores = signal<VendedorAdmin[]>([]);
  readonly error = signal<string | null>(null);
  readonly pendientes = computed(() => this.vendedores().filter(v => !v.aprobado).length);

  ngOnInit(): void {
    this.service.getVendedores().subscribe({
      next: v => this.vendedores.set([...v].sort((a, b) => Number(a.aprobado) - Number(b.aprobado))),
      error: err => this.error.set(mensajeError(err))
    });
  }

  cambiar(v: VendedorAdmin, aprobado: boolean): void {
    this.service.aprobarVendedor(v.id, aprobado).subscribe({
      next: act => this.vendedores.update(l => l.map(x => x.id === act.id ? act : x)),
      error: err => this.error.set(mensajeError(err))
    });
  }
}
