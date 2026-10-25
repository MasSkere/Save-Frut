import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { OrdenService } from '../../../core/services/orden.service';
import { mensajeError } from '../../../core/services/auth.service';
import { Orden } from '../../../shared/models/orden.model';
import { CurrencyPENPipe } from '../../../shared/pipes/currency-pen.pipe';
import { EstadoBadgeComponent } from '../../../shared/components/estado-badge.component';
import { IconComponent } from '../../../shared/components/icon.component';

@Component({
  selector: 'app-mis-pedidos',
  standalone: true,
  imports: [RouterLink, DatePipe, CurrencyPENPipe, EstadoBadgeComponent, IconComponent],
  template: `
    <div class="max-w-4xl mx-auto px-4 pt-8 pb-4">
      <h1 class="text-4xl font-bold text-brand-700">Mis pedidos</h1>
      @if (!loading() && pagadas() > 0) {
        <p class="mt-1 text-gray-600">Has rescatado fruta en <span class="cifra">{{ pagadas() }}</span> {{ pagadas() === 1 ? 'compra' : 'compras' }}.</p>
      }

      @if (error()) {
        <div class="mt-6 bg-red-50 border border-red-200 rounded-card p-4 text-sm text-red-800 flex items-start gap-2" role="alert">
          <app-icon nombre="alert" [tamano]="18" class="mt-0.5 text-red-600" />{{ error() }}
        </div>
      }

      @if (loading()) {
        <div class="flex justify-center py-24" role="status">
          <div class="animate-spin rounded-full h-10 w-10 border-2 border-brand-100 border-t-brand-500" aria-hidden="true"></div>
          <span class="sr-only">Cargando tus pedidos…</span>
        </div>
      } @else {
        <div class="mt-6 space-y-4">
          @for (o of ordenes(); track o.id) {
            <article class="bg-surface-card border rounded-card overflow-hidden" [attr.aria-labelledby]="'orden-' + o.id"
                     [class]="o.estadoOrden === 'PENDIENTE_PAGO' ? 'border-amber-300' : 'border-surface-border'">
              @if (o.estadoOrden === 'PENDIENTE_PAGO') {
                <div class="bg-amber-50 border-b border-amber-200 px-5 py-3 flex flex-wrap items-center justify-between gap-3">
                  <p class="text-sm font-medium text-amber-900">Falta pagar esta orden. El stock sigue reservado para ti.</p>
                  <div class="flex flex-wrap gap-2">
                    <button type="button" class="btn-secondary !py-1.5" [disabled]="verificando() === o.id" (click)="verificar(o)">
                      {{ verificando() === o.id ? 'Verificando…' : 'Ya pagué, verificar' }}
                    </button>
                    <button type="button" class="btn-primary !py-1.5" [disabled]="pagando() === o.id" (click)="pagar(o)">
                      {{ pagando() === o.id ? 'Abriendo…' : 'Pagar con Mercado Pago' }}
                    </button>
                  </div>
                </div>
              }
              <div class="p-5">
                <header class="flex flex-wrap items-start justify-between gap-3">
                  <div class="min-w-0">
                    <h2 [id]="'orden-' + o.id" class="text-lg font-bold text-gray-900 cifra">Orden #{{ o.id }}</h2>
                    <p class="mt-0.5 flex flex-wrap items-center gap-x-3 gap-y-1 text-sm text-gray-600">
                      <span class="cifra">{{ o.fechaOrden | date: "d 'de' MMMM, HH:mm" }}</span>
                      @if (o.direccionEnvio === 'Recojo en tienda') {
                        <span class="inline-flex items-center gap-1"><app-icon nombre="store" [tamano]="15" />Recojo en tienda</span>
                      } @else {
                        <span class="inline-flex items-center gap-1 min-w-0">
                          <app-icon nombre="truck" [tamano]="15" />
                          <span class="truncate max-w-[16rem]">{{ o.direccionEnvio }}{{ o.distritoEnvio ? ', ' + o.distritoEnvio : '' }}</span>
                        </span>
                      }
                    </p>
                  </div>
                  <app-estado-badge [estado]="o.estadoOrden" />
                </header>

                <ul class="mt-4 text-sm text-gray-800 space-y-1.5 cifra">
                  @for (i of o.items; track i.productoId) {
                    <li class="flex justify-between gap-3">
                      <span><span class="text-gray-500">{{ i.cantidad }} ×</span> {{ i.nombreProducto }}</span>
                      <span>{{ i.subtotalItem | currencyPEN }}</span>
                    </li>
                  }
                </ul>

                <footer class="mt-4 pt-3 border-t border-surface-border flex flex-wrap items-baseline justify-between gap-2 text-sm cifra">
                  <p class="text-gray-600">
                    Envío {{ o.costoEnvio ? (o.costoEnvio | currencyPEN) : 'gratis' }}
                    @if (o.tarifaVariedad > 0) { · Tarifa por variedad {{ o.tarifaVariedad | currencyPEN }} }
                  </p>
                  <p class="text-gray-700">Total <strong class="font-display text-lg font-bold text-gray-900 ml-1">{{ o.total | currencyPEN }}</strong></p>
                </footer>
              </div>
            </article>
          } @empty {
            @if (!error()) {
              <div class="bg-surface-card border border-surface-border rounded-card px-6 py-14 text-center">
                <span class="mx-auto w-16 h-16 rounded-full bg-brand-50 text-brand-500 flex items-center justify-center" aria-hidden="true">
                  <app-icon nombre="package" [tamano]="30" [trazo]="1.5" />
                </span>
                <p class="mt-4 font-display text-2xl font-bold text-gray-900">Aún no tienes pedidos</p>
                <p class="mt-1.5 text-gray-600">Tu primera compra salva fruta y suma Eco-Puntos.</p>
                <a routerLink="/productos" class="btn-primary mt-6">Ver lo que vence pronto</a>
              </div>
            }
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
  readonly pagadas = computed(() =>
    this.ordenes().filter(o => o.estadoOrden !== 'PENDIENTE_PAGO' && o.estadoOrden !== 'CANCELADO').length);

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
    // La pestaña se abre en el clic para que el navegador no la bloquee; recibe la URL cuando llega la preferencia.
    const pestana = window.open('', '_blank');
    this.ordenService.crearPreferencia(orden.id).subscribe({
      next: p => this.ordenService.irAPagar(p, pestana),
      error: err => {
        pestana?.close();
        this.pagando.set(null);
        this.error.set(mensajeError(err, 'No se pudo iniciar el pago'));
      }
    });
  }
}
