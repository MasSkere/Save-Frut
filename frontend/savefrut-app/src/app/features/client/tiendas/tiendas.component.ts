import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CatalogoService } from '../../../core/services/catalogo.service';
import { Vendedor } from '../../../shared/models/producto.model';
import { IMAGEN_TIENDA } from '../../../shared/utils/producto.utils';

@Component({
  selector: 'app-tiendas',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="max-w-7xl mx-auto px-4 py-8">
      <h1 class="text-2xl font-bold text-brand-700 mb-1">Tiendas aliadas</h1>
      <p class="text-sm text-gray-500 mb-6">Vendedores minoristas que ofrecen sus productos próximos a vencer.</p>
      @if (loading()) {
        <div class="flex justify-center py-20"><div class="animate-spin rounded-full h-12 w-12 border-b-2 border-brand-500"></div></div>
      } @else {
        <div class="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
          @for (v of vendedores(); track v.id) {
            <a [routerLink]="['/tienda', v.id]" class="card-sf p-5 flex gap-4 hover:shadow-lg transition-shadow">
              <img [src]="v.logoUrl || imagenTienda" alt="" class="w-16 h-16 rounded-full object-cover bg-gray-100 shrink-0" />
              <div class="min-w-0">
                <h2 class="font-semibold text-gray-900">{{ v.razonSocial }}</h2>
                <p class="text-sm text-gray-600 truncate">{{ v.direccion }}</p>
                <p class="text-xs text-gray-500 mt-1">{{ v.nombreDistrito }} · {{ v.horario }}</p>
              </div>
            </a>
          } @empty {
            <p class="text-gray-500">Aún no hay tiendas aprobadas.</p>
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
  readonly imagenTienda = IMAGEN_TIENDA;

  ngOnInit(): void {
    this.catalogoService.getVendedores().subscribe({
      next: v => { this.vendedores.set(v); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }
}
