import { Component, OnInit, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { CatalogoService } from '../../../core/services/catalogo.service';
import { Producto, Vendedor } from '../../../shared/models/producto.model';
import { ProductCardComponent } from '../../../shared/components/product-card/product-card.component';
import { IMAGEN_TIENDA } from '../../../shared/utils/producto.utils';
import { MapaComponent } from '../../../shared/components/mapa.component';

@Component({
  selector: 'app-tienda-detalle',
  standalone: true,
  imports: [RouterLink, ProductCardComponent, MapaComponent],
  template: `
    @if (loading()) {
      <div class="flex justify-center py-20"><div class="animate-spin rounded-full h-12 w-12 border-b-2 border-brand-500"></div></div>
    } @else if (!vendedor()) {
      <div class="max-w-7xl mx-auto px-4 py-8 text-center">
        <p class="text-red-600">Tienda no encontrada</p>
        <a routerLink="/tiendas" class="text-brand-600 underline">Ver tiendas</a>
      </div>
    } @else {
      @let tienda = vendedor()!;
      <section class="bg-surface-card border-b border-surface-border">
        <div class="max-w-7xl mx-auto px-4 py-8 flex flex-col md:flex-row items-center gap-6">
          <img [src]="tienda.logoUrl || imagenTienda" alt="" class="w-24 h-24 rounded-full object-cover bg-gray-100" />
          <div class="text-center md:text-left">
            <h1 class="text-2xl font-bold text-gray-900">{{ tienda.razonSocial }}</h1>
            <p class="text-gray-600 mt-1">{{ tienda.direccion }} · {{ tienda.nombreDistrito }}</p>
            <p class="text-sm text-gray-500">Horario: {{ tienda.horario }}</p>
            @if (tienda.latitud && tienda.longitud) {
              <a class="text-sm text-brand-600 underline" target="_blank" rel="noopener"
                 [href]="'https://www.google.com/maps/search/?api=1&query=' + tienda.latitud + ',' + tienda.longitud">Ver en Google Maps</a>
            }
          </div>
        </div>
        @if (tienda.latitud && tienda.longitud) {
          <div class="max-w-7xl mx-auto px-4 pb-6">
            <app-mapa [lat]="tienda.latitud" [lng]="tienda.longitud" [alto]="220" />
          </div>
        }
      </section>
      <section class="max-w-7xl mx-auto px-4 py-8">
        <h2 class="text-xl font-bold text-brand-700 mb-6">Productos disponibles ({{ productos().length }})</h2>
        <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 md:gap-6">
          @for (producto of productos(); track producto.id) {
            <app-product-card [producto]="producto" />
          } @empty {
            <p class="text-gray-500 col-span-full text-center py-12">Esta tienda no tiene productos por vencer en este momento.</p>
          }
        </div>
      </section>
    }
  `
})
export class TiendaDetalleComponent implements OnInit {
  private readonly catalogoService = inject(CatalogoService);

  readonly id = input.required<string>();
  readonly vendedor = signal<Vendedor | null>(null);
  readonly productos = signal<Producto[]>([]);
  readonly loading = signal(true);
  readonly imagenTienda = IMAGEN_TIENDA;

  ngOnInit(): void {
    const vendedorId = Number(this.id());
    forkJoin({
      vendedor: this.catalogoService.getVendedor(vendedorId),
      productos: this.catalogoService.getProductosPorVendedor(vendedorId)
    }).subscribe({
      next: r => {
        this.vendedor.set(r.vendedor);
        this.productos.set(r.productos);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }
}
