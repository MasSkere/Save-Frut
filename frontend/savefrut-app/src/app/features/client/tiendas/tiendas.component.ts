import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CatalogoService } from '../../../core/services/catalogo.service';
import { Vendedor } from '../../../shared/models/producto.model';
import { iniciales, tieneImagen } from '../../../shared/utils/producto.utils';
import { IconComponent } from '../../../shared/components/icon.component';

interface GrupoDistrito {
  distrito: string;
  tiendas: Vendedor[];
}

@Component({
  selector: 'app-tiendas',
  standalone: true,
  imports: [RouterLink, IconComponent],
  template: `
    <div class="max-w-5xl mx-auto px-4 pt-8 pb-4">
      <h1 class="text-4xl font-bold text-brand-700">Tiendas aliadas</h1>
      <p class="mt-1 text-gray-600 max-w-prose">Mercados y bodegas de Lima que publican aquí lo que está por vencer. Búscalas por distrito.</p>

      @if (loading()) {
        <div class="flex justify-center py-24" role="status">
          <div class="animate-spin rounded-full h-10 w-10 border-2 border-brand-100 border-t-brand-500" aria-hidden="true"></div>
          <span class="sr-only">Cargando tiendas…</span>
        </div>
      } @else if (error()) {
        <div class="mt-8 bg-red-50 border border-red-200 rounded-card p-6 text-center text-red-700" role="alert">
          <p>No pudimos cargar las tiendas. Revisa tu conexión e inténtalo de nuevo.</p>
          <button type="button" (click)="cargar()" class="btn-secondary mt-4">Intentar de nuevo</button>
        </div>
      } @else if (grupos().length === 0) {
        <p class="mt-10 text-center text-gray-600">Aún no hay tiendas aprobadas.</p>
      } @else {
        <div class="mt-8 space-y-8">
          @for (g of grupos(); track g.distrito) {
            <section [attr.aria-labelledby]="'d-' + $index">
              <h2 [id]="'d-' + $index" class="flex items-baseline gap-2 text-xl font-bold text-gray-900">
                {{ g.distrito }}
                <span class="font-sans text-sm font-normal text-gray-500 cifra">{{ g.tiendas.length }} {{ g.tiendas.length === 1 ? 'tienda' : 'tiendas' }}</span>
              </h2>
              <ul class="mt-3 bg-surface-card rounded-card border border-surface-border divide-y divide-surface-border overflow-hidden">
                @for (v of g.tiendas; track v.id) {
                  <li>
                    <a [routerLink]="['/tienda', v.id]" class="group flex items-center gap-4 p-4 hover:bg-brand-50/60 transition-colors">
                      @if (tieneImagen(v.logoUrl)) {
                        <img [src]="v.logoUrl" alt="" class="w-12 h-12 rounded-full object-cover bg-gray-100 shrink-0" />
                      } @else {
                        <span class="w-12 h-12 rounded-full bg-brand-100 text-brand-700 font-display font-bold flex items-center justify-center shrink-0" aria-hidden="true">{{ iniciales(v.razonSocial) }}</span>
                      }
                      <div class="min-w-0 flex-1">
                        <h3 class="font-semibold text-gray-900 truncate">{{ v.razonSocial }}</h3>
                        <div class="mt-0.5 flex flex-col sm:flex-row sm:gap-4 text-sm text-gray-600">
                          @if (v.direccion) {
                            <span class="inline-flex items-center gap-1.5 min-w-0"><app-icon nombre="pin" [tamano]="15" class="text-gray-400" /><span class="truncate">{{ v.direccion }}</span></span>
                          }
                          @if (v.horario) {
                            <span class="inline-flex items-center gap-1.5 shrink-0"><app-icon nombre="clock" [tamano]="15" class="text-gray-400" />{{ v.horario }}</span>
                          }
                        </div>
                      </div>
                      <app-icon nombre="arrowRight" [tamano]="18" class="text-gray-400 group-hover:text-brand-600 group-hover:translate-x-0.5 transition-transform" />
                    </a>
                  </li>
                }
              </ul>
            </section>
          }
        </div>
      }
    </div>
  `
})
export class TiendasComponent implements OnInit {
  private readonly catalogoService = inject(CatalogoService);
  readonly vendedores = signal<Vendedor[]>([]);
  readonly loading = signal(true);
  readonly error = signal(false);
  readonly tieneImagen = tieneImagen;
  readonly iniciales = iniciales;

  /** Agrupadas por distrito: así el comprador encuentra lo que le queda cerca. */
  readonly grupos = computed<GrupoDistrito[]>(() => {
    const mapa = new Map<string, Vendedor[]>();
    for (const v of this.vendedores()) {
      const distrito = v.nombreDistrito || 'Otros distritos';
      mapa.set(distrito, [...(mapa.get(distrito) ?? []), v]);
    }
    return [...mapa.entries()]
      .map(([distrito, tiendas]) => ({ distrito, tiendas }))
      .sort((a, b) => b.tiendas.length - a.tiendas.length || a.distrito.localeCompare(b.distrito));
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(false);
    this.catalogoService.getVendedores().subscribe({
      next: v => { this.vendedores.set(v); this.loading.set(false); },
      error: () => { this.error.set(true); this.loading.set(false); }
    });
  }
}
