import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { VendedorPanelService } from '../../core/services/vendedor-panel.service';
import { mensajeError } from '../../core/services/auth.service';
import { OrdenVendedor } from '../../shared/models/panel.model';
import { CurrencyPENPipe } from '../../shared/pipes/currency-pen.pipe';
import { EstadoBadgeComponent } from '../../shared/components/estado-badge.component';

@Component({
  selector: 'app-vendedor-ordenes',
  standalone: true,
  imports: [DatePipe, CurrencyPENPipe, EstadoBadgeComponent],
  template: `
    <h1 class="text-2xl font-bold text-gray-900 mb-1">Pedidos</h1>
    <p class="text-sm text-gray-500 mb-6">Órdenes pagadas que incluyen productos de tu tienda.</p>
    @if (error()) {
      <div class="mb-4 bg-red-50 border border-red-200 rounded-card p-3 text-sm text-red-700" role="alert">{{ error() }}</div>
    }
    <div class="space-y-4">
      @for (o of ordenes(); track o.id) {
        <article class="card-sf p-5">
          <header class="flex flex-wrap justify-between items-start gap-2">
            <div>
              <h2 class="font-semibold">Orden #{{ o.id }} · {{ o.nombreCliente ?? 'Cliente' }}</h2>
              <p class="text-xs text-gray-500">{{ o.fechaOrden | date: 'dd/MM/yyyy HH:mm' }} · {{ o.direccionEnvio }}, {{ o.distritoEnvio }}
                @if (o.latitudEnvio && o.longitudEnvio) {
                  · <a class="text-brand-600 underline" target="_blank" rel="noopener"
                       [href]="'https://www.google.com/maps/search/?api=1&query=' + o.latitudEnvio + ',' + o.longitudEnvio">Ver en Google Maps</a>
                }
              </p>
            </div>
            <app-estado-badge [estado]="o.estadoOrden" />
          </header>
          <ul class="mt-3 text-sm space-y-1">
            @for (i of o.items; track i.productoId) {
              <li class="flex justify-between"><span>{{ i.cantidad }} × {{ i.nombreProducto }}</span><span class="tabular-nums">{{ i.subtotalItem | currencyPEN }}</span></li>
            }
          </ul>
          <footer class="mt-3 pt-3 border-t border-surface-border flex justify-between items-center">
            <span class="text-sm">Tu ingreso: <strong>{{ o.totalVendedor | currencyPEN }}</strong></span>
            @if (o.estadoOrden === 'PAGADO') {
              <button type="button" class="btn-primary" (click)="preparar(o)">Marcar como preparado</button>
            }
          </footer>
        </article>
      } @empty {
        <p class="text-center text-gray-500 py-16">{{ loading() ? 'Cargando…' : 'Aún no tienes pedidos pagados.' }}</p>
      }
    </div>
  `
})
export class VendedorOrdenesComponent implements OnInit {
  private readonly service = inject(VendedorPanelService);
  readonly ordenes = signal<OrdenVendedor[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.service.getOrdenes().subscribe({
      next: o => { this.ordenes.set(o); this.loading.set(false); },
      error: err => { this.error.set(mensajeError(err)); this.loading.set(false); }
    });
  }

  preparar(orden: OrdenVendedor): void {
    this.service.marcarPreparado(orden.id).subscribe({
      next: actualizada => this.ordenes.update(l => l.map(o => o.id === orden.id ? { ...o, estadoOrden: actualizada.estadoOrden } : o)),
      error: err => this.error.set(mensajeError(err))
    });
  }
}
