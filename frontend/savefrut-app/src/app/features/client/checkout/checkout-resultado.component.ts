import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Subscription, timer } from 'rxjs';
import { OrdenService } from '../../../core/services/orden.service';
import { mensajeError } from '../../../core/services/auth.service';
import { ETIQUETA_ESTADO, Orden } from '../../../shared/models/orden.model';
import { MisPuntos } from '../../../shared/models/panel.model';
import { CurrencyPENPipe } from '../../../shared/pipes/currency-pen.pipe';

type Estado = 'verificando' | 'esperando' | 'aprobado' | 'rechazado' | 'error';

const INTERVALO_MS = 3000;
const MAX_INTENTOS = 200; // ~10 minutos esperando el pago

/**
 * Resultado del pago. Se llega por la back_url de Mercado Pago o desde el checkout ("Ya pagué").
 * Mientras la orden siga PENDIENTE_PAGO consulta al backend cada pocos segundos; al aprobarse muestra la boleta
 * y los Eco-Puntos ganados (los acredita ms-puntos de forma asíncrona vía RabbitMQ).
 */
@Component({
  selector: 'app-checkout-resultado',
  standalone: true,
  imports: [RouterLink, CurrencyPENPipe, DatePipe],
  template: `
    <div class="max-w-2xl mx-auto px-4 py-12">
      @switch (estado()) {
        @case ('verificando') {
          <div class="text-center py-10">
            <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-brand-500 mx-auto"></div>
            <p class="mt-4 text-gray-600">Verificando tu pago con Mercado Pago…</p>
          </div>
        }
        @case ('esperando') {
          <div class="text-center py-10" role="status">
            <div class="animate-pulse text-6xl" aria-hidden="true">💳</div>
            <h1 class="text-2xl font-bold text-gray-900 mt-4">Esperando tu pago</h1>
            <p class="text-gray-600 mt-2">Mercado Pago aún no confirma el pago de la orden <strong>#{{ ordenId }}</strong>.
              Esta página se actualiza sola y mostrará tu boleta apenas se apruebe.</p>
            @if (aviso()) {
              <p class="mt-4 p-3 rounded-button bg-amber-50 text-amber-800 text-sm" role="alert">{{ aviso() }}</p>
            }
            <div class="mt-6 flex gap-3 justify-center">
              <button type="button" class="btn-primary" [disabled]="verificando()" (click)="verificarAhora()">
                {{ verificando() ? 'Verificando…' : 'Ya pagué, verificar' }}
              </button>
              <a routerLink="/mis-pedidos" class="btn-secondary">Mis pedidos</a>
            </div>
          </div>
        }
        @case ('aprobado') {
          <div class="text-center">
            <div class="text-6xl" aria-hidden="true">🎉</div>
            <h1 class="text-2xl font-bold text-brand-700 mt-4">¡Pago aprobado!</h1>
            <p class="text-gray-600 mt-1">Gracias por salvar fruta con SaveFrut.</p>
          </div>

          @if (orden(); as o) {
            <section class="card-sf p-6 mt-8" aria-label="Boleta de compra">
              <div class="flex justify-between items-start border-b pb-4">
                <div>
                  <h2 class="font-bold text-lg">Boleta de compra</h2>
                  <p class="text-sm text-gray-500">Orden #{{ o.id }} · {{ o.fechaPago ?? o.fechaOrden | date:'dd/MM/yyyy HH:mm' }}</p>
                </div>
                <span class="px-3 py-1 rounded-full text-sm font-medium bg-brand-50 text-brand-700">{{ etiqueta[o.estadoOrden] }}</span>
              </div>
              <table class="w-full text-sm mt-4">
                <thead class="text-gray-500 text-left">
                  <tr><th class="pb-2 font-medium">Producto</th><th class="pb-2 font-medium text-center">Cant.</th><th class="pb-2 font-medium text-right">P. unit.</th><th class="pb-2 font-medium text-right">Subtotal</th></tr>
                </thead>
                <tbody>
                  @for (i of o.items; track i.productoId) {
                    <tr class="border-t">
                      <td class="py-2">{{ i.nombreProducto }}</td>
                      <td class="py-2 text-center">{{ i.cantidad }}</td>
                      <td class="py-2 text-right">{{ i.precioUnitario | currencyPEN }}</td>
                      <td class="py-2 text-right">{{ i.subtotalItem | currencyPEN }}</td>
                    </tr>
                  }
                </tbody>
              </table>
              <dl class="mt-4 border-t pt-4 space-y-1 text-sm">
                <div class="flex justify-between"><dt>Subtotal</dt><dd>{{ o.subtotal | currencyPEN }}</dd></div>
                <div class="flex justify-between"><dt>Envío</dt><dd>{{ o.costoEnvio | currencyPEN }}</dd></div>
                @if (o.tarifaVariedad > 0) {
                  <div class="flex justify-between"><dt>Tarifa por variedad de tiendas</dt><dd>{{ o.tarifaVariedad | currencyPEN }}</dd></div>
                }
                <div class="flex justify-between font-bold text-base pt-2 border-t"><dt>Total pagado</dt><dd>{{ o.total | currencyPEN }}</dd></div>
              </dl>
              <p class="text-sm text-gray-500 mt-4">Entrega: {{ o.direccionEnvio }}{{ o.distritoEnvio ? ', ' + o.distritoEnvio : '' }}</p>
            </section>
          }

          <section class="card-sf p-6 mt-6 bg-brand-50" aria-label="Eco-Puntos">
            <h2 class="font-semibold text-brand-700">🌱 Eco-Puntos</h2>
            @if (puntosGanados() !== null) {
              <p class="mt-2 text-sm">Ganaste <strong class="text-lg">{{ puntosGanados() }}</strong> Eco-Puntos con esta compra
                y salvaste <strong>{{ frutas() }}</strong> unidades de fruta.</p>
              <p class="mt-1 text-sm">Total acumulado: <strong>{{ puntos()?.puntosAcumulados }}</strong> · Nivel <strong>{{ puntos()?.nivel }}</strong></p>
            } @else {
              <p class="mt-2 text-sm text-gray-600">Salvaste {{ frutas() }} unidades de fruta. Acreditando tus Eco-Puntos…</p>
            }
          </section>

          <div class="mt-6 flex gap-3 justify-center">
            <a routerLink="/mis-puntos" class="btn-primary">Ver mis Eco-Puntos</a>
            <a routerLink="/mis-pedidos" class="btn-secondary">Mis pedidos</a>
          </div>
        }
        @case ('rechazado') {
          <div class="text-center">
            <div class="text-6xl" aria-hidden="true">⚠️</div>
            <h1 class="text-2xl font-bold text-red-700 mt-4">El pago no se completó</h1>
            <p class="text-gray-600 mt-2">Tu orden #{{ ordenId }} sigue reservada. Puedes intentar pagar nuevamente.</p>
            <a routerLink="/mis-pedidos" class="btn-primary mt-6">Reintentar desde mis pedidos</a>
          </div>
        }
        @default {
          <div class="text-center">
            <h1 class="text-2xl font-bold text-red-700">No pudimos verificar el pago</h1>
            <p class="text-gray-600 mt-2">{{ mensaje() }}</p>
            <a routerLink="/mis-pedidos" class="btn-primary mt-6">Ir a mis pedidos</a>
          </div>
        }
      }
    </div>
  `
})
export class CheckoutResultadoComponent implements OnInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly ordenService = inject(OrdenService);

  readonly etiqueta = ETIQUETA_ESTADO;
  readonly estado = signal<Estado>('verificando');
  readonly orden = signal<Orden | null>(null);
  readonly mensaje = signal('');
  readonly frutas = signal(0);
  readonly puntos = signal<MisPuntos | null>(null);
  readonly puntosGanados = signal<number | null>(null);
  readonly aviso = signal<string | null>(null);
  readonly verificando = signal(false);
  ordenId = 0;

  private sondeo?: Subscription;

  ngOnInit(): void {
    const q = this.route.snapshot.queryParamMap;
    this.ordenId = Number(q.get('ordenId') ?? q.get('external_reference'));
    const paymentId = q.get('payment_id') ?? q.get('collection_id');
    const status = q.get('status') ?? q.get('collection_status');

    // Con payment_id (volvió desde la back_url de MP) se confirma ese pago; si no, se concilia buscándolo en MP
    const peticion = paymentId && paymentId !== 'null'
      ? this.ordenService.confirmarPago(this.ordenId, paymentId)
      : this.ordenService.verificarPago(this.ordenId);
    peticion.subscribe({
      next: orden => {
        if (orden.estadoOrden === 'PENDIENTE_PAGO' && status === 'rejected') {
          this.estado.set('rechazado');
        } else {
          this.procesar(orden);
        }
      },
      error: err => this.fallar(err)
    });
  }

  ngOnDestroy(): void {
    this.sondeo?.unsubscribe();
  }

  verificarAhora(): void {
    this.aviso.set(null);
    this.verificando.set(true);
    this.ordenService.verificarPago(this.ordenId).subscribe({
      next: orden => {
        this.verificando.set(false);
        if (orden.estadoOrden === 'PENDIENTE_PAGO') {
          this.aviso.set('Mercado Pago aún no registra un pago aprobado para esta orden. Si tu pago fue rechazado, '
            + 'en la pestaña de Mercado Pago elige "Pagar de otra forma" (por ejemplo, tarjeta).');
        }
        this.procesar(orden);
      },
      error: err => {
        this.verificando.set(false);
        this.fallar(err);
      }
    });
  }

  private procesar(orden: Orden): void {
    this.orden.set(orden);
    this.frutas.set(orden.items.reduce((acc, i) => acc + i.cantidad, 0));
    if (orden.estadoOrden === 'PENDIENTE_PAGO') {
      this.estado.set('esperando');
      this.esperarPago();
      return;
    }
    this.sondeo?.unsubscribe();
    this.estado.set('aprobado');
    this.esperarPuntos();
  }

  /** Consulta cada pocos segundos si Mercado Pago ya aprobó el pago. */
  private esperarPago(): void {
    if (this.sondeo && !this.sondeo.closed) {
      return;
    }
    this.sondeo = timer(INTERVALO_MS, INTERVALO_MS).subscribe(intento => {
      if (intento >= MAX_INTENTOS) {
        this.sondeo?.unsubscribe();
        return;
      }
      this.ordenService.verificarPago(this.ordenId).subscribe({
        next: orden => {
          if (orden.estadoOrden !== 'PENDIENTE_PAGO') {
            this.procesar(orden);
          }
        },
        error: () => {}
      });
    });
  }

  /** ms-puntos acredita los puntos al consumir el evento OrdenPagada; se reintenta hasta verlos. */
  private esperarPuntos(): void {
    this.sondeo = timer(0, 2000).subscribe(intento => {
      if (intento >= 15) {
        this.sondeo?.unsubscribe();
        return;
      }
      this.ordenService.misPuntos().subscribe({
        next: p => {
          const movimiento = p.movimientos.find(m => m.ordenId === this.ordenId);
          if (movimiento) {
            this.puntos.set(p);
            this.puntosGanados.set(movimiento.puntos);
            this.sondeo?.unsubscribe();
          }
        },
        error: () => {}
      });
    });
  }

  private fallar(err: unknown): void {
    this.sondeo?.unsubscribe();
    this.mensaje.set(mensajeError(err));
    this.estado.set('error');
  }
}
