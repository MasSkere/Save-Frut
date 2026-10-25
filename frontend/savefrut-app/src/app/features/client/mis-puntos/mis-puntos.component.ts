import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { OrdenService } from '../../../core/services/orden.service';
import { MisPuntos } from '../../../shared/models/panel.model';
import { CurrencyPENPipe } from '../../../shared/pipes/currency-pen.pipe';

const NIVELES = [
  { nombre: 'Semilla', minimo: 0, icono: '🌱' },
  { nombre: 'Brote', minimo: 100, icono: '🌿' },
  { nombre: 'Héroe Verde', minimo: 300, icono: '🌳' },
  { nombre: 'Guardián del Planeta', minimo: 1000, icono: '🌍' }
];

@Component({
  selector: 'app-mis-puntos',
  standalone: true,
  imports: [RouterLink, DatePipe, CurrencyPENPipe],
  template: `
    <div class="max-w-4xl mx-auto px-4 py-8">
      <h1 class="text-2xl font-bold text-brand-700 mb-1">Mis Eco-Puntos</h1>
      <p class="text-sm text-gray-500 mb-6">Ganas 1 punto por cada S/ 1 y 5 puntos por cada fruta salvada.</p>

      @if (puntos(); as p) {
        <section class="rounded-card p-6 bg-gradient-to-br from-brand-600 to-brand-800 text-white shadow-soft">
          <div class="flex flex-wrap items-center justify-between gap-4">
            <div>
              <p class="text-brand-200 text-sm">Nivel actual</p>
              <p class="text-2xl font-bold">{{ icono() }} {{ p.nivel }}</p>
            </div>
            <div class="text-right">
              <p class="text-brand-200 text-sm">Eco-Puntos</p>
              <p class="text-4xl font-bold tabular-nums">{{ p.puntosAcumulados }}</p>
            </div>
          </div>
          @if (p.puntosParaSiguienteNivel !== null) {
            <div class="mt-5">
              <div class="h-2 bg-brand-900/50 rounded-full overflow-hidden">
                <div class="h-full bg-urgency-medium rounded-full" [style.width.%]="progreso()"></div>
              </div>
              <p class="text-xs text-brand-200 mt-1">Te faltan {{ p.puntosParaSiguienteNivel }} puntos para el siguiente nivel</p>
            </div>
          }
        </section>

        <div class="grid grid-cols-3 gap-4 mt-6 text-center">
          <div class="card-sf p-4"><p class="text-2xl font-bold text-brand-600 tabular-nums">{{ p.frutasSalvadas }}</p><p class="text-xs text-gray-500">frutas salvadas</p></div>
          <div class="card-sf p-4"><p class="text-2xl font-bold text-brand-600 tabular-nums">{{ p.totalCompras }}</p><p class="text-xs text-gray-500">compras</p></div>
          <div class="card-sf p-4"><p class="text-2xl font-bold text-brand-600 tabular-nums">{{ p.montoTotalGastado | currencyPEN }}</p><p class="text-xs text-gray-500">invertido en ahorro</p></div>
        </div>

        <section class="card-sf p-6 mt-6">
          <h2 class="font-semibold mb-3">Historial</h2>
          <ul class="divide-y divide-surface-border">
            @for (m of p.movimientos; track m.ordenId) {
              <li class="py-2 flex justify-between text-sm">
                <span>Orden #{{ m.ordenId }} · {{ m.fecha | date: 'dd/MM/yyyy' }} · {{ m.frutasSalvadas }} frutas</span>
                <span class="font-semibold text-brand-600">+{{ m.puntos }}</span>
              </li>
            } @empty {
              <li class="py-6 text-center text-gray-500 text-sm">
                Aún no tienes movimientos. <a routerLink="/productos" class="text-brand-600 underline">Salva tu primera fruta</a>.
              </li>
            }
          </ul>
        </section>
      } @else {
        <div class="flex justify-center py-20"><div class="animate-spin rounded-full h-12 w-12 border-b-2 border-brand-500"></div></div>
      }
    </div>
  `
})
export class MisPuntosComponent implements OnInit {
  private readonly ordenService = inject(OrdenService);
  readonly puntos = signal<MisPuntos | null>(null);

  readonly icono = computed(() => NIVELES.find(n => n.nombre === this.puntos()?.nivel)?.icono ?? '🌱');
  readonly progreso = computed(() => {
    const p = this.puntos();
    if (!p || p.puntosParaSiguienteNivel === null) return 100;
    const actual = NIVELES.find(n => n.nombre === p.nivel)?.minimo ?? 0;
    const siguiente = p.puntosAcumulados + p.puntosParaSiguienteNivel;
    return ((p.puntosAcumulados - actual) / (siguiente - actual)) * 100;
  });

  ngOnInit(): void {
    this.ordenService.misPuntos().subscribe(p => this.puntos.set(p));
  }
}
