import { Component, computed, input } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { VentaDiaria } from '../models/orden.model';

/** Gráfico de barras simple (ventas por día) sin dependencias externas. */
@Component({
  selector: 'app-bar-chart',
  standalone: true,
  imports: [DecimalPipe],
  template: `
    <div class="flex items-end gap-2 h-44" role="img" [attr.aria-label]="'Ventas de los últimos ' + datos().length + ' días'">
      @for (d of barras(); track d.fecha) {
        <div class="flex-1 flex flex-col items-center justify-end h-full gap-1 min-w-0">
          <span class="text-[10px] text-gray-600 tabular-nums">{{ d.total | number: '1.0-0' }}</span>
          <div class="w-full max-w-10 rounded-t bg-brand-500 transition-all" [style.height.%]="d.altura"
               [title]="'S/ ' + (d.total | number: '1.2-2') + ' · ' + d.cantidad + ' órdenes'"></div>
          <span class="text-[10px] text-gray-500">{{ d.etiqueta }}</span>
        </div>
      }
    </div>
  `
})
export class BarChartComponent {
  readonly datos = input.required<VentaDiaria[]>();

  readonly barras = computed(() => {
    const max = Math.max(1, ...this.datos().map(d => Number(d.total)));
    return this.datos().map(d => ({
      ...d,
      total: Number(d.total),
      altura: Math.max(2, (Number(d.total) / max) * 85),
      etiqueta: new Date(d.fecha + 'T00:00:00').toLocaleDateString('es-PE', { weekday: 'short', day: 'numeric' })
    }));
  });
}
