import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { VendedorPanelService } from '../../core/services/vendedor-panel.service';
import { Alerta, PerfilVendedor, ResumenProductos, ResumenVendedor } from '../../shared/models/panel.model';
import { CurrencyPENPipe } from '../../shared/pipes/currency-pen.pipe';
import { StatCardComponent } from '../../shared/components/stat-card.component';
import { BarChartComponent } from '../../shared/components/bar-chart.component';

@Component({
  selector: 'app-vendedor-dashboard',
  standalone: true,
  imports: [RouterLink, CurrencyPENPipe, StatCardComponent, BarChartComponent],
  template: `
    <h1 class="text-2xl font-bold text-gray-900 mb-6">Resumen de mi tienda</h1>
    @if (perfil() && !perfil()!.aprobado) {
      <div class="mb-6 bg-amber-50 border border-amber-200 rounded-card p-4 text-sm text-amber-800" role="status">
        ⏳ Tu tienda está <strong>pendiente de aprobación</strong>. Podrás publicar productos cuando un administrador la apruebe.
      </div>
    }
    @if (ventas() && inventario()) {
      <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <app-stat-card titulo="Ingresos hoy" [valor]="ventas()!.ingresosHoy | currencyPEN" />
        <app-stat-card titulo="Ingresos 7 días" [valor]="ventas()!.ingresosSemana | currencyPEN" />
        <app-stat-card titulo="Pedidos por preparar" [valor]="ventas()!.ordenesPorPreparar" borde="border-blue-500" />
        <app-stat-card titulo="Frutas salvadas" [valor]="ventas()!.unidadesVendidas" detalle="unidades vendidas" borde="border-urgency-medium" />
      </div>

      <div class="grid lg:grid-cols-3 gap-6 mt-6">
        <section class="card-sf p-6 lg:col-span-2">
          <h2 class="font-semibold mb-4">Ventas de los últimos 7 días (S/)</h2>
          <app-bar-chart [datos]="ventas()!.ventasSemana" />
        </section>
        <section class="card-sf p-6">
          <h2 class="font-semibold mb-3">Inventario</h2>
          <dl class="space-y-2 text-sm">
            <div class="flex justify-between"><dt>Productos activos</dt><dd class="font-semibold">{{ inventario()!.productosActivos }}</dd></div>
            <div class="flex justify-between"><dt>Stock bajo (&lt; 10)</dt><dd class="font-semibold text-amber-600">{{ inventario()!.productosStockBajo }}</dd></div>
            <div class="flex justify-between"><dt>Vencen en ≤ 2 días</dt><dd class="font-semibold text-orange-600">{{ inventario()!.productosPorVencer }}</dd></div>
            <div class="flex justify-between"><dt>Vencidos</dt><dd class="font-semibold text-red-600">{{ inventario()!.productosVencidos }}</dd></div>
          </dl>
          <a routerLink="/vendedor/productos" class="btn-secondary w-full mt-4">Gestionar productos</a>
        </section>
      </div>

      <section class="card-sf p-6 mt-6">
        <h2 class="font-semibold mb-3">Alertas</h2>
        <ul class="space-y-2">
          @for (a of alertas(); track a.productoId + a.tipo) {
            <li class="text-sm flex items-center gap-2">
              <span class="w-2 h-2 rounded-full" [class]="a.tipo === 'VENCIDO' ? 'bg-red-500' : a.tipo === 'POR_VENCER' ? 'bg-orange-500' : 'bg-amber-400'"></span>
              {{ a.mensaje }}
            </li>
          } @empty {
            <li class="text-sm text-gray-500">Sin alertas. ¡Todo en orden!</li>
          }
        </ul>
      </section>
    } @else {
      <div class="flex justify-center py-20"><div class="animate-spin rounded-full h-12 w-12 border-b-2 border-brand-500"></div></div>
    }
  `
})
export class VendedorDashboardComponent implements OnInit {
  private readonly service = inject(VendedorPanelService);
  readonly ventas = signal<ResumenVendedor | null>(null);
  readonly inventario = signal<ResumenProductos | null>(null);
  readonly alertas = signal<Alerta[]>([]);
  readonly perfil = signal<PerfilVendedor | null>(null);

  ngOnInit(): void {
    this.service.getPerfil().subscribe(p => this.perfil.set(p));
    forkJoin({
      ventas: this.service.getResumenVentas(),
      inventario: this.service.getResumenProductos(),
      alertas: this.service.getAlertas()
    }).subscribe(r => {
      this.ventas.set(r.ventas);
      this.inventario.set(r.inventario);
      this.alertas.set(r.alertas);
    });
  }
}
