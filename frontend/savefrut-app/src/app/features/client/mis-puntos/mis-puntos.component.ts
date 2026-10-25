import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { OrdenService } from '../../../core/services/orden.service';
import { MisPuntos } from '../../../shared/models/panel.model';
import { CurrencyPENPipe } from '../../../shared/pipes/currency-pen.pipe';

const NIVELES = [
  { nombre: 'Semilla', minimo: 0 },
  { nombre: 'Brote', minimo: 100 },
  { nombre: 'Héroe Verde', minimo: 300 },
  { nombre: 'Guardián del Planeta', minimo: 1000 }
];

@Component({
  selector: 'app-mis-puntos',
  standalone: true,
  imports: [RouterLink, DatePipe, CurrencyPENPipe],
  template: `
    <div class="max-w-4xl mx-auto px-4 pt-8 pb-4">
      <h1 class="text-4xl font-bold text-brand-700">Mis Eco-Puntos</h1>
      <p class="mt-1 text-gray-600">Ganas 1 punto por cada S/ 1 y 5 puntos por cada fruta que salvas.</p>

      @if (error()) {
        <div class="mt-6 bg-red-50 border border-red-200 rounded-card p-6 text-center text-red-700" role="alert">
          <p>No pudimos cargar tus Eco-Puntos. Revisa tu conexión e inténtalo de nuevo.</p>
          <button type="button" (click)="cargar()" class="btn-secondary mt-4">Intentar de nuevo</button>
        </div>
      } @else if (puntos(); as p) {
        <section class="mt-6 rounded-2xl bg-brand-900 text-brand-50 p-6 sm:p-8" aria-labelledby="nivel-titulo">
          <div class="flex flex-wrap items-end justify-between gap-6">
            <div>
              <p class="text-sm text-brand-300">Tu nivel</p>
              <h2 id="nivel-titulo" class="mt-1 text-3xl sm:text-4xl font-bold text-white">{{ p.nivel }}</h2>
            </div>
            <div class="text-right">
              <p class="text-sm text-brand-300">Eco-Puntos</p>
              <p class="font-display text-5xl sm:text-6xl font-bold text-white leading-none cifra">{{ p.puntosAcumulados }}</p>
            </div>
          </div>

          <ol class="mt-8 grid grid-cols-4 gap-1.5" aria-label="Niveles">
            @for (n of niveles; track n.nombre; let idx = $index) {
              <li [attr.aria-current]="idx === indiceNivel() ? 'step' : null">
                <div class="h-1.5 rounded-full overflow-hidden bg-white/10">
                  <div class="h-full w-full rounded-full bg-brand-300 barra"
                       [style.transform]="'scaleX(' + (idx < indiceNivel() ? 1 : idx === indiceNivel() ? progreso() / 100 : 0) + ')'"></div>
                </div>
                <p class="mt-2 text-xs leading-tight" [class]="idx <= indiceNivel() ? 'text-white font-semibold' : 'text-brand-300/70'">{{ n.nombre }}</p>
                <p class="text-[0.7rem] text-brand-300/70 cifra">{{ n.minimo }} pts</p>
              </li>
            }
          </ol>
          @if (p.puntosParaSiguienteNivel !== null) {
            <p class="mt-4 text-sm text-brand-200">Te faltan <strong class="text-white cifra">{{ p.puntosParaSiguienteNivel }}</strong> puntos para el siguiente nivel.</p>
          } @else {
            <p class="mt-4 text-sm text-brand-200">Llegaste al nivel más alto.</p>
          }
        </section>

        <dl class="mt-6 grid grid-cols-3 divide-x divide-surface-border bg-surface-card border border-surface-border rounded-card">
          <div class="p-4 sm:p-5">
            <dt class="text-xs sm:text-sm text-gray-600">Frutas salvadas</dt>
            <dd class="mt-1 font-display text-2xl sm:text-3xl font-bold text-brand-700 cifra">{{ p.frutasSalvadas }}</dd>
          </div>
          <div class="p-4 sm:p-5">
            <dt class="text-xs sm:text-sm text-gray-600">Compras</dt>
            <dd class="mt-1 font-display text-2xl sm:text-3xl font-bold text-gray-900 cifra">{{ p.totalCompras }}</dd>
          </div>
          <div class="p-4 sm:p-5">
            <dt class="text-xs sm:text-sm text-gray-600">Gastado en fruta rescatada</dt>
            <dd class="mt-1 font-display text-2xl sm:text-3xl font-bold text-gray-900 cifra">{{ p.montoTotalGastado | currencyPEN }}</dd>
          </div>
        </dl>

        <section class="mt-6 bg-surface-card border border-surface-border rounded-card" aria-labelledby="historial-titulo">
          <h2 id="historial-titulo" class="px-5 pt-5 text-xl font-bold text-gray-900">Historial</h2>
          <ul class="mt-2 divide-y divide-surface-border">
            @for (m of p.movimientos; track m.ordenId) {
              <li class="px-5 py-3.5 flex items-center justify-between gap-4 text-sm cifra">
                <div>
                  <p class="font-medium text-gray-900">Orden #{{ m.ordenId }}</p>
                  <p class="text-gray-600">{{ m.fecha | date: "d 'de' MMMM" }} · {{ m.frutasSalvadas }} {{ m.frutasSalvadas === 1 ? 'fruta salvada' : 'frutas salvadas' }}</p>
                </div>
                <span class="font-display text-lg font-bold text-brand-600">+{{ m.puntos }}</span>
              </li>
            } @empty {
              <li class="px-5 py-10 text-center text-gray-600">
                Aún no tienes movimientos. <a routerLink="/productos" class="text-brand-600 font-medium underline">Salva tu primera fruta</a>.
              </li>
            }
          </ul>
        </section>
      } @else {
        <div class="flex justify-center py-24" role="status">
          <div class="animate-spin rounded-full h-10 w-10 border-2 border-brand-100 border-t-brand-500" aria-hidden="true"></div>
          <span class="sr-only">Cargando tus Eco-Puntos…</span>
        </div>
      }
    </div>
  `,
  styles: `
    .barra {
      transform-origin: left;
      transition: transform 700ms var(--ease-out);
    }
    @starting-style {
      .barra {
        transform: scaleX(0);
      }
    }
  `
})
export class MisPuntosComponent implements OnInit {
  private readonly ordenService = inject(OrdenService);
  readonly puntos = signal<MisPuntos | null>(null);
  readonly error = signal(false);
  readonly niveles = NIVELES;

  readonly indiceNivel = computed(() => Math.max(0, NIVELES.findIndex(n => n.nombre === this.puntos()?.nivel)));
  readonly progreso = computed(() => {
    const p = this.puntos();
    if (!p || p.puntosParaSiguienteNivel === null) return 100;
    const actual = NIVELES.find(n => n.nombre === p.nivel)?.minimo ?? 0;
    const siguiente = p.puntosAcumulados + p.puntosParaSiguienteNivel;
    return ((p.puntosAcumulados - actual) / (siguiente - actual)) * 100;
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.error.set(false);
    this.ordenService.misPuntos().subscribe({
      next: p => this.puntos.set(p),
      error: () => this.error.set(true)
    });
  }
}
