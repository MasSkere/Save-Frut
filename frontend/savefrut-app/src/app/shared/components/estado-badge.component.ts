import { Component, computed, input } from '@angular/core';
import { ETIQUETA_ESTADO, EstadoOrden } from '../models/orden.model';

const CLASES: Record<string, string> = {
  PENDIENTE_PAGO: 'bg-amber-100 text-amber-800',
  PAGADO: 'bg-blue-100 text-blue-800',
  PREPARADO: 'bg-indigo-100 text-indigo-800',
  ENTREGADO: 'bg-brand-100 text-brand-700',
  CANCELADO: 'bg-gray-200 text-gray-600'
};

@Component({
  selector: 'app-estado-badge',
  standalone: true,
  template: `<span class="inline-block text-xs font-semibold px-2 py-1 rounded-full whitespace-nowrap" [class]="clase()">{{ etiqueta() }}</span>`
})
export class EstadoBadgeComponent {
  readonly estado = input.required<string>();
  readonly etiqueta = computed(() => ETIQUETA_ESTADO[this.estado() as EstadoOrden] ?? this.estado());
  readonly clase = computed(() => CLASES[this.estado()] ?? 'bg-gray-100 text-gray-700');
}
