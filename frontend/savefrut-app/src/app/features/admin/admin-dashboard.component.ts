import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin, catchError, of } from 'rxjs';
import { AdminService } from '../../core/services/admin.service';
import { Alerta, DashboardAdmin, EstadisticasPuntos, ResumenProductos } from '../../shared/models/panel.model';
import { ETIQUETA_ESTADO, EstadoOrden } from '../../shared/models/orden.model';
import { CurrencyPENPipe } from '../../shared/pipes/currency-pen.pipe';
import { StatCardComponent } from '../../shared/components/stat-card.component';
import { BarChartComponent } from '../../shared/components/bar-chart.component';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [RouterLink, DecimalPipe, CurrencyPENPipe, StatCardComponent, BarChartComponent],
  templateUrl: './admin-dashboard.component.html'
})
export class AdminDashboardComponent implements OnInit {
  private readonly service = inject(AdminService);

  readonly dashboard = signal<DashboardAdmin | null>(null);
  readonly inventario = signal<ResumenProductos | null>(null);
  readonly puntos = signal<EstadisticasPuntos | null>(null);
  readonly alertas = signal<Alerta[]>([]);

  readonly estados = computed(() => {
    const conteo = this.dashboard()?.ordenesPorEstado ?? {};
    const total = Object.values(conteo).reduce((a, b) => a + b, 0) || 1;
    return Object.entries(conteo).map(([estado, cantidad]) => ({
      estado,
      etiqueta: ETIQUETA_ESTADO[estado as EstadoOrden] ?? estado,
      cantidad,
      porcentaje: (cantidad / total) * 100
    }));
  });

  ngOnInit(): void {
    forkJoin({
      dashboard: this.service.getDashboard(),
      inventario: this.service.getResumenProductos(),
      alertas: this.service.getAlertas(),
      puntos: this.service.getEstadisticasPuntos().pipe(catchError(() => of(null)))
    }).subscribe(r => {
      this.dashboard.set(r.dashboard);
      this.inventario.set(r.inventario);
      this.alertas.set(r.alertas);
      this.puntos.set(r.puntos);
    });
  }
}
