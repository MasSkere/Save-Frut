import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../core/services/admin.service';
import { mensajeError } from '../../core/services/auth.service';
import { ESTADOS_ORDEN, ETIQUETA_ESTADO, Orden } from '../../shared/models/orden.model';
import { CurrencyPENPipe } from '../../shared/pipes/currency-pen.pipe';
import { EstadoBadgeComponent } from '../../shared/components/estado-badge.component';

@Component({
  selector: 'app-admin-ordenes',
  standalone: true,
  imports: [FormsModule, DatePipe, CurrencyPENPipe, EstadoBadgeComponent],
  template: `
    <div class="flex flex-wrap items-center justify-between gap-3 mb-6">
      <h1 class="text-2xl font-bold text-gray-900">Órdenes</h1>
      <label class="text-sm flex items-center gap-2">Estado
        <select class="input-sf !w-auto" [ngModel]="filtro()" (ngModelChange)="filtrar($event)">
          <option value="">Todos</option>
          @for (e of estados; track e) { <option [value]="e">{{ etiquetas[e] }}</option> }
        </select>
      </label>
    </div>
    @if (error()) {
      <div class="mb-4 bg-red-50 border border-red-200 rounded-card p-3 text-sm text-red-700" role="alert">{{ error() }}</div>
    }
    <div class="card-sf overflow-x-auto">
      <table class="tabla-sf">
        <thead><tr><th>#</th><th>Fecha</th><th>Cliente</th><th>Productos</th><th class="text-right">Total</th><th>Estado</th><th>Cambiar a</th><th></th></tr></thead>
        <tbody>
          @for (o of ordenes(); track o.id) {
            <tr>
              <td class="font-medium">{{ o.id }}</td>
              <td class="whitespace-nowrap">{{ o.fechaOrden | date: 'dd/MM HH:mm' }}</td>
              <td>{{ o.nombreCliente ?? ('Cliente #' + o.clienteId) }}</td>
              <td class="max-w-56 truncate" [title]="resumen(o)">{{ resumen(o) }}</td>
              <td class="text-right tabular-nums">{{ o.total | currencyPEN }}</td>
              <td><app-estado-badge [estado]="o.estadoOrden" /></td>
              <td>
                @if (o.estadoOrden !== 'CANCELADO' && o.estadoOrden !== 'ENTREGADO') {
                  <label class="sr-only" [for]="'estado-' + o.id">Nuevo estado de la orden {{ o.id }}</label>
                  <select [id]="'estado-' + o.id" class="input-sf !w-auto !py-1" [ngModel]="o.estadoOrden" (ngModelChange)="cambiarEstado(o, $event)">
                    @for (e of estados; track e) { <option [value]="e">{{ etiquetas[e] }}</option> }
                  </select>
                }
              </td>
              <td>
                @if (o.estadoOrden !== 'CANCELADO' && o.estadoOrden !== 'ENTREGADO') {
                  <button type="button" class="text-red-600 text-xs hover:underline" (click)="cancelar(o)">Cancelar</button>
                }
              </td>
            </tr>
          } @empty {
            <tr><td colspan="8" class="text-center text-gray-500 py-8">{{ loading() ? 'Cargando…' : 'No hay órdenes.' }}</td></tr>
          }
        </tbody>
      </table>
    </div>
  `
})
export class AdminOrdenesComponent implements OnInit {
  private readonly service = inject(AdminService);
  readonly estados = ESTADOS_ORDEN;
  readonly etiquetas = ETIQUETA_ESTADO;
  readonly ordenes = signal<Orden[]>([]);
  readonly filtro = signal('');
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.cargar();
  }

  filtrar(estado: string): void {
    this.filtro.set(estado);
    this.cargar();
  }

  resumen(o: Orden): string {
    return o.items.map(i => `${i.cantidad}× ${i.nombreProducto}`).join(', ');
  }

  cambiarEstado(o: Orden, estado: string): void {
    if (estado === o.estadoOrden) return;
    this.service.cambiarEstadoOrden(o.id, estado).subscribe({
      next: act => this.reemplazar(act),
      error: err => { this.error.set(mensajeError(err)); this.cargar(); }
    });
  }

  cancelar(o: Orden): void {
    this.service.cancelarOrden(o.id, 'Cancelada por el administrador').subscribe({
      next: act => this.reemplazar(act),
      error: err => this.error.set(mensajeError(err))
    });
  }

  private cargar(): void {
    this.loading.set(true);
    this.service.getOrdenes(this.filtro() || undefined).subscribe({
      next: o => { this.ordenes.set(o); this.loading.set(false); },
      error: err => { this.error.set(mensajeError(err)); this.loading.set(false); }
    });
  }

  private reemplazar(act: Orden): void {
    this.error.set(null);
    this.ordenes.update(l => l.map(o => o.id === act.id ? act : o));
  }
}
