import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { OrdenService } from '../../../core/services/orden.service';
import { mensajeError } from '../../../core/services/auth.service';
import { Orden } from '../../../shared/models/orden.model';
import { CurrencyPENPipe } from '../../../shared/pipes/currency-pen.pipe';
import { EstadoBadgeComponent } from '../../../shared/components/estado-badge.component';

@Component({
  selector: 'app-mis-pedidos',
  standalone: true,
  imports: [RouterLink, DatePipe, CurrencyPENPipe, EstadoBadgeComponent],
  template: `
    <div class="max-w-4xl mx-auto px-4 py-8">
      <h1 class="text-2xl font-bold text-brand-700 mb-6">Mis pedidos</h1>
      @if (error()) {
        <div class="mb-4 bg-red-50 border border-red-200 rounded-card p-3 text-sm text-red-700" role="alert">{{ error() }}</div>
      }
      @if (loading()) {
        <div class="flex justify-center py-20"><div class="animate-spin rounded-full h-12 w-12 border-b-2 border-brand-500"></div></div>
      } @else {
        <div class="space-y-4">
          @for (o of ordenes(); track o.id) {
            <article class="card-sf p-5">
              <header class="flex flex-wrap items-center justify-between gap-2">
                <div>
                  <h2 class="font-semibold">Orden #{{ o.id }}</h2>
                  <p class="text-xs text-gray-500">{{ o.fechaOrden | date: 'dd/MM/yyyy HH:mm' }} · {{ o.direccionEnvio }}, {{ o.distritoEnvio }}</p>
                </div>
                <app-estado-badge [estado]="o.estadoOrden" />
              </header>
              <ul class="mt-3 text-sm text-gray-700 space-y-1">
                @for (i of o.items; track i.productoId) {
                  <li class="flex justify-between"><span>{{ i.cantidad }} × {{ i.nombreProducto }}</span><span class="tabular-nums">{{ i.subtotalItem | currencyPEN }}</span></li>
                }
              </ul>
              <footer class="mt-3 pt-3 border-t border-surface-border flex flex-wrap items-center justify-between gap-2">
                <p class="text-sm text-gray-600">
                  Envío {{ o.costoEnvio | currencyPEN }}
                  @if (o.tarifaVariedad > 0) { · Variedad {{ o.tarifaVariedad | currencyPEN }} }
                  · <strong class="text-gray-900">Total {{ o.total | currencyPEN }}</strong>
                </p>
                @if (o.estadoOrden === 'PENDIENTE_PAGO') {
                  <div class="flex gap-2">
                    <button type="button" class="btn-secondary" [disabled]="verificando() === o.id" (click)="verificar(o)">
                      {{ verificando() === o.id ? 'Verificando…' : 'Ya pagué, verificar' }}
                    </button>
                    <button type="button" class="btn-primary" [disabled]="pagando() === o.id" (click)="pagar(o)">
                      {{ pagando() === o.id ? 'Redirigiendo…' : 'Pagar con Mercado Pago' }}
                    </button>
                  </div>
                }
              </footer>
            </article>
          } @empty {
            <div class="text-center py-16 text-gray-500">
              <p>Aún no tienes pedidos.</p>
              <a routerLink="/productos" class="btn-primary mt-4">Explorar catálogo</a>
            </div>
          }
        </div>
      }
    </div>
  `
})
export class MisPedidosComponent implements OnInit {
  private readonly ordenService = inject(OrdenService);

  readonly ordenes = signal<Orden[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly pagando = signal<number | null>(null);
  readonly verificando = signal<number | null>(null);

  ngOnInit(): void {
    this.ordenService.misOrdenes().subscribe({
      next: o => { this.ordenes.set(o); this.loading.set(false); },
      error: err => { this.error.set(mensajeError(err)); this.loading.set(false); }
    });
  }

  verificar(orden: Orden): void {
    this.error.set(null);
    this.verificando.set(orden.id);
    this.ordenService.verificarPago(orden.id).subscribe({
      next: actualizada => {
        this.verificando.set(null);
        this.ordenes.update(l => l.map(o => o.id === actualizada.id ? actualizada : o));
        if (actualizada.estadoOrden === 'PENDIENTE_PAGO') {
          this.error.set(`Mercado Pago aún no registra un pago aprobado para la orden #${orden.id}.`);
        }
      },
      error: err => {
        this.verificando.set(null);
        this.error.set(mensajeError(err, 'No se pudo verificar el pago'));
      }
    });
  }

  pagar(orden: Orden): void {
    this.error.set(null);
    this.pagando.set(orden.id);
    this.ordenService.crearPreferencia(orden.id).subscribe({
      next: p => window.location.href = p.initPoint || p.sandboxInitPoint,
      error: err => {
        this.pagando.set(null);
        this.error.set(mensajeError(err, 'No se pudo iniciar el pago'));
      }
    });
  }
}
