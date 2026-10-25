import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Subscription, timer } from 'rxjs';
import { OrdenService } from '../../../core/services/orden.service';
import { CatalogoService } from '../../../core/services/catalogo.service';
import { mensajeError } from '../../../core/services/auth.service';
import { Orden } from '../../../shared/models/orden.model';
import { MisPuntos } from '../../../shared/models/panel.model';
import { Vendedor } from '../../../shared/models/producto.model';
import { CurrencyPENPipe } from '../../../shared/pipes/currency-pen.pipe';
import { EstadoBadgeComponent } from '../../../shared/components/estado-badge.component';
import { IconComponent } from '../../../shared/components/icon.component';
import { CLAVE_AHORRO_ORDEN } from './checkout.component';
import { direccionConDistrito } from '../../../shared/utils/producto.utils';

type Estado = 'verificando' | 'esperando' | 'aprobado' | 'rechazado' | 'error';

const INTERVALO_MS = 3000;
const MAX_INTENTOS = 200; // ~10 minutos esperando el pago
const MAX_INTENTOS_PUNTOS = 15; // ~30 segundos esperando a ms-puntos

/**
 * Resultado del pago. Se llega por la back_url de Mercado Pago o desde el checkout.
 * Mientras la orden siga PENDIENTE_PAGO consulta al backend cada pocos segundos; al aprobarse muestra la boleta
 * y los Eco-Puntos ganados (los acredita ms-puntos de forma asíncrona vía RabbitMQ).
 */
@Component({
  selector: 'app-checkout-resultado',
  standalone: true,
  imports: [RouterLink, CurrencyPENPipe, DatePipe, EstadoBadgeComponent, IconComponent],
  template: `
    <div class="max-w-2xl mx-auto px-4 pt-12 pb-4">
      @switch (estado()) {
        @case ('verificando') {
          <div class="text-center py-16" role="status">
            <div class="mx-auto animate-spin rounded-full h-10 w-10 border-2 border-brand-100 border-t-brand-500" aria-hidden="true"></div>
            <p class="mt-4 text-gray-700">Verificando tu pago con Mercado Pago…</p>
          </div>
        }
        @case ('esperando') {
          <div class="text-center py-6" role="status">
            <span class="mx-auto w-16 h-16 rounded-full bg-brand-50 text-brand-600 flex items-center justify-center" aria-hidden="true">
              <app-icon nombre="card" [tamano]="30" [trazo]="1.5" />
            </span>
            @if (!agotado()) {
              <h1 class="mt-5 text-3xl font-bold text-gray-900">Esperando tu pago</h1>
              <p class="mt-2 text-gray-700 max-w-md mx-auto">Completa el pago de la orden <strong class="cifra">#{{ ordenId }}</strong> en la pestaña de Mercado Pago.
                Esta página se actualiza sola y te muestra la boleta apenas se apruebe.</p>
            } @else {
              <h1 class="mt-5 text-3xl font-bold text-gray-900">Aún no vemos tu pago</h1>
              <p class="mt-2 text-gray-700 max-w-md mx-auto">Dejamos de revisar automáticamente. Tu orden <strong class="cifra">#{{ ordenId }}</strong> sigue reservada:
                si ya pagaste, verifícalo; si no, vuelve a abrir Mercado Pago.</p>
            }
            @if (aviso()) {
              <p class="mt-5 p-3 rounded-button bg-amber-50 border border-amber-200 text-amber-900 text-sm text-left" role="alert">{{ aviso() }}</p>
            }
            <div class="mt-7 flex flex-wrap gap-3 justify-center">
              <button type="button" class="btn-primary" [disabled]="verificando()" (click)="verificarAhora()">
                {{ verificando() ? 'Verificando…' : 'Ya pagué, verificar' }}
              </button>
              <button type="button" class="btn-secondary" [disabled]="abriendoPago()" (click)="pagarDeNuevo()">
                {{ abriendoPago() ? 'Abriendo…' : 'Abrir Mercado Pago otra vez' }}
              </button>
            </div>
            <a routerLink="/mis-pedidos" class="mt-4 inline-block text-sm font-medium text-brand-600 hover:underline">Ir a mis pedidos</a>
          </div>
        }
        @case ('aprobado') {
          @if (orden(); as o) {
            <div class="text-center">
              <span class="check mx-auto w-14 h-14 rounded-full bg-brand-500 text-white flex items-center justify-center" aria-hidden="true">
                <app-icon nombre="check" [tamano]="28" [trazo]="2.5" />
              </span>
              <p class="mt-4 text-sm font-semibold text-brand-600">Pago aprobado</p>
              <h1 class="mt-1 text-4xl font-bold text-brand-700 leading-tight">
                Salvaste {{ frutas() }} {{ frutas() === 1 ? 'fruta' : 'frutas' }}
                de {{ tiendasOrden() }} {{ tiendasOrden() === 1 ? 'tienda' : 'tiendas' }}
              </h1>
              @if (ahorro() > 0) {
                <p class="mt-2 text-lg text-gray-700">y ahorraste <strong class="font-semibold text-brand-700 cifra">{{ ahorro() | currencyPEN }}</strong> frente al precio normal.</p>
              }
            </div>

            <section class="mt-8 rounded-card bg-brand-900 text-brand-50 p-5 sm:p-6 flex items-center justify-between gap-4" aria-label="Eco-Puntos">
              @if (puntosGanados() !== null) {
                <div>
                  <p class="text-sm text-brand-200">Eco-Puntos de esta compra</p>
                  <p class="font-display text-4xl font-bold text-white cifra">+{{ puntosGanados() }}</p>
                </div>
                <div class="text-right text-sm text-brand-200">
                  <p>Acumulas <strong class="text-white cifra">{{ puntos()?.puntosAcumulados }}</strong></p>
                  <p>Nivel <strong class="text-white">{{ puntos()?.nivel }}</strong></p>
                </div>
              } @else if (puntosAgotado()) {
                <p class="text-sm text-brand-100">Tus Eco-Puntos de esta compra aparecerán en <a routerLink="/mis-puntos" class="underline text-white">Mis Eco-Puntos</a> en unos minutos.</p>
              } @else {
                <p class="text-sm text-brand-100 flex items-center gap-3" role="status">
                  <span class="animate-spin rounded-full h-4 w-4 border-2 border-white/30 border-t-white" aria-hidden="true"></span>
                  Acreditando tus Eco-Puntos…
                </p>
              }
            </section>

            <section class="mt-6 bg-surface-card border border-surface-border rounded-card p-5 sm:p-6" aria-labelledby="boleta-titulo">
              <div class="flex justify-between items-start gap-4 pb-4 border-b border-surface-border">
                <div>
                  <h2 id="boleta-titulo" class="text-xl font-bold text-gray-900">Boleta de compra</h2>
                  <p class="mt-0.5 text-sm text-gray-600 cifra">Orden #{{ o.id }} · {{ o.fechaPago ?? o.fechaOrden | date:'dd/MM/yyyy HH:mm' }}</p>
                </div>
                <app-estado-badge [estado]="o.estadoOrden" />
              </div>
              <table class="w-full text-sm mt-2 cifra">
                <caption class="sr-only">Productos de la orden</caption>
                <thead class="text-gray-600 text-left">
                  <tr>
                    <th class="py-2 font-medium">Producto</th>
                    <th class="py-2 font-medium text-right">Cant.</th>
                    <th class="py-2 font-medium text-right hidden sm:table-cell">P. unit.</th>
                    <th class="py-2 font-medium text-right">Subtotal</th>
                  </tr>
                </thead>
                <tbody>
                  @for (i of o.items; track i.productoId) {
                    <tr class="border-t border-surface-border">
                      <td class="py-2.5 pr-3">{{ i.nombreProducto }}</td>
                      <td class="py-2.5 text-right">{{ i.cantidad }}</td>
                      <td class="py-2.5 text-right hidden sm:table-cell">{{ i.precioUnitario | currencyPEN }}</td>
                      <td class="py-2.5 text-right">{{ i.subtotalItem | currencyPEN }}</td>
                    </tr>
                  }
                </tbody>
              </table>
              <dl class="mt-2 border-t border-surface-border pt-4 space-y-1.5 text-sm cifra">
                <div class="flex justify-between"><dt class="text-gray-600">Subtotal</dt><dd>{{ o.subtotal | currencyPEN }}</dd></div>
                <div class="flex justify-between"><dt class="text-gray-600">Envío</dt><dd>{{ o.costoEnvio ? (o.costoEnvio | currencyPEN) : 'Gratis' }}</dd></div>
                @if (o.tarifaVariedad > 0) {
                  <div class="flex justify-between"><dt class="text-gray-600">Tarifa por variedad</dt><dd>{{ o.tarifaVariedad | currencyPEN }}</dd></div>
                }
                <div class="flex justify-between items-baseline pt-3 mt-2 border-t border-surface-border">
                  <dt class="font-semibold text-gray-900">Total pagado</dt>
                  <dd class="font-display text-2xl font-bold text-brand-700">{{ o.total | currencyPEN }}</dd>
                </div>
              </dl>

              <div class="mt-5 pt-4 border-t border-surface-border text-sm">
                @if (esRecojo()) {
                  <h3 class="font-semibold text-gray-900">Recógelo en</h3>
                  <ul class="mt-2 space-y-3">
                    @for (t of tiendasRecojo(); track t.id) {
                      <li>
                        <p class="font-medium text-gray-900">{{ t.razonSocial }}</p>
                        <p class="text-gray-700">{{ direccionConDistrito(t.direccion, t.nombreDistrito) }}</p>
                        @if (t.horario) { <p class="text-gray-600">{{ t.horario }}</p> }
                      </li>
                    } @empty {
                      <li class="text-gray-700">En las tiendas de tu pedido. Revisa sus direcciones en <a routerLink="/tiendas" class="text-brand-600 underline">Tiendas aliadas</a>.</li>
                    }
                  </ul>
                } @else {
                  <h3 class="font-semibold text-gray-900">Entrega en</h3>
                  <p class="mt-1 text-gray-700">{{ o.direccionEnvio }}{{ o.distritoEnvio ? ', ' + o.distritoEnvio : '' }}</p>
                }
              </div>
            </section>

            <div class="mt-6 flex flex-wrap gap-3 justify-center">
              <a routerLink="/productos" class="btn-primary">Seguir rescatando</a>
              <a routerLink="/mis-pedidos" class="btn-secondary">Mis pedidos</a>
            </div>
          }
        }
        @case ('rechazado') {
          <div class="text-center py-6">
            <span class="mx-auto w-16 h-16 rounded-full bg-red-50 text-red-600 flex items-center justify-center" aria-hidden="true">
              <app-icon nombre="alert" [tamano]="30" [trazo]="1.75" />
            </span>
            <h1 class="mt-5 text-3xl font-bold text-gray-900">El pago no se completó</h1>
            <p class="mt-2 text-gray-700">Tu orden <strong class="cifra">#{{ ordenId }}</strong> sigue reservada. Puedes intentarlo de nuevo con otro medio de pago.</p>
            <div class="mt-7 flex flex-wrap gap-3 justify-center">
              <button type="button" class="btn-primary" [disabled]="abriendoPago()" (click)="pagarDeNuevo()">
                {{ abriendoPago() ? 'Abriendo…' : 'Intentar el pago de nuevo' }}
              </button>
              <a routerLink="/mis-pedidos" class="btn-secondary">Mis pedidos</a>
            </div>
          </div>
        }
        @default {
          <div class="text-center py-6">
            <span class="mx-auto w-16 h-16 rounded-full bg-red-50 text-red-600 flex items-center justify-center" aria-hidden="true">
              <app-icon nombre="alert" [tamano]="30" [trazo]="1.75" />
            </span>
            <h1 class="mt-5 text-3xl font-bold text-gray-900">No pudimos verificar el pago</h1>
            <p class="mt-2 text-gray-700">{{ mensaje() }}</p>
            <p class="mt-1 text-sm text-gray-600">Si ya pagaste, tu orden se actualizará en Mis pedidos.</p>
            <a routerLink="/mis-pedidos" class="btn-primary mt-6">Ir a mis pedidos</a>
          </div>
        }
      }
    </div>
  `,
  styles: `
    .check {
      transition: transform 450ms var(--ease-out), opacity 200ms ease;
    }
    @starting-style {
      .check {
        transform: scale(0.6);
        opacity: 0;
      }
    }
  `
})
export class CheckoutResultadoComponent implements OnInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly ordenService = inject(OrdenService);
  private readonly catalogoService = inject(CatalogoService);

  readonly estado = signal<Estado>('verificando');
  readonly orden = signal<Orden | null>(null);
  readonly mensaje = signal('');
  readonly frutas = signal(0);
  readonly puntos = signal<MisPuntos | null>(null);
  readonly puntosGanados = signal<number | null>(null);
  readonly puntosAgotado = signal(false);
  readonly aviso = signal<string | null>(null);
  readonly verificando = signal(false);
  readonly agotado = signal(false);
  readonly abriendoPago = signal(false);
  readonly ahorro = signal(0);
  readonly vendedores = signal<Vendedor[]>([]);
  ordenId = 0;
  readonly direccionConDistrito = direccionConDistrito;

  readonly tiendasOrden = computed(() => new Set(this.orden()?.items.map(i => i.vendedorId) ?? []).size);
  readonly esRecojo = computed(() => this.orden()?.direccionEnvio === 'Recojo en tienda');
  readonly tiendasRecojo = computed(() => {
    const ids = new Set(this.orden()?.items.map(i => i.vendedorId) ?? []);
    return this.vendedores().filter(v => ids.has(v.id));
  });

  private sondeo?: Subscription;

  ngOnInit(): void {
    const q = this.route.snapshot.queryParamMap;
    this.ordenId = Number(q.get('ordenId') ?? q.get('external_reference'));
    const paymentId = q.get('payment_id') ?? q.get('collection_id');
    const status = q.get('status') ?? q.get('collection_status');
    this.ahorro.set(Number(sessionStorage.getItem(CLAVE_AHORRO_ORDEN + this.ordenId) ?? 0) || 0);
    this.catalogoService.getVendedores().subscribe({ next: v => this.vendedores.set(v), error: () => {} });

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

  /** Vuelve a abrir Mercado Pago para la misma orden (misma Idempotency-Key, sin cobro duplicado). */
  pagarDeNuevo(): void {
    this.abriendoPago.set(true);
    const pestana = window.open('', '_blank');
    this.ordenService.crearPreferencia(this.ordenId).subscribe({
      next: preferencia => {
        this.abriendoPago.set(false);
        this.agotado.set(false);
        this.ordenService.irAPagar(preferencia, pestana);
        this.estado.set('esperando');
        this.esperarPago();
      },
      error: err => {
        pestana?.close();
        this.abriendoPago.set(false);
        this.aviso.set(mensajeError(err, 'No se pudo abrir Mercado Pago. Inténtalo de nuevo en unos segundos.'));
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
        this.agotado.set(true);
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
      if (intento >= MAX_INTENTOS_PUNTOS) {
        this.sondeo?.unsubscribe();
        this.puntosAgotado.set(true);
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
