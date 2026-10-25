import { Component, OnInit, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { CatalogoService } from '../../../core/services/catalogo.service';
import { Producto, Vendedor } from '../../../shared/models/producto.model';
import { ProductCardComponent } from '../../../shared/components/product-card/product-card.component';
import { direccionConDistrito, iniciales, tieneImagen } from '../../../shared/utils/producto.utils';
import { MapaComponent } from '../../../shared/components/mapa.component';
import { IconComponent } from '../../../shared/components/icon.component';
import { GoogleMapsLoaderService } from '../../../core/services/google-maps-loader.service';

@Component({
  selector: 'app-tienda-detalle',
  standalone: true,
  imports: [RouterLink, ProductCardComponent, MapaComponent, IconComponent],
  template: `
    @if (loading()) {
      <div class="flex justify-center py-24" role="status">
        <div class="animate-spin rounded-full h-10 w-10 border-2 border-brand-100 border-t-brand-500" aria-hidden="true"></div>
        <span class="sr-only">Cargando tienda…</span>
      </div>
    } @else if (!vendedor()) {
      <div class="max-w-xl mx-auto px-4 py-20 text-center">
        <p class="font-display text-2xl font-bold text-brand-700">No encontramos esta tienda</p>
        <p class="mt-2 text-gray-600">Puede que ya no publique en SaveFrut.</p>
        <a routerLink="/tiendas" class="btn-primary mt-6">Ver tiendas aliadas</a>
      </div>
    } @else {
      @let tienda = vendedor()!;
      @let conUbicacion = !!(tienda.latitud && tienda.longitud);
      @let conMapa = conUbicacion && mapasDisponibles;
      <section class="bg-surface-card border-b border-surface-border">
        <div class="max-w-7xl mx-auto px-4 py-8 md:py-10 grid gap-8" [class]="conMapa ? 'lg:grid-cols-[1fr_26rem] lg:items-center' : ''">
          <div class="flex flex-col sm:flex-row sm:items-center gap-5">
            @if (tieneImagen(tienda.logoUrl)) {
              <img [src]="tienda.logoUrl" alt="" class="w-20 h-20 rounded-full object-cover bg-gray-100 shrink-0" />
            } @else {
              <span class="w-20 h-20 rounded-full bg-brand-100 text-brand-700 font-display text-2xl font-bold flex items-center justify-center shrink-0" aria-hidden="true">{{ iniciales(tienda.razonSocial) }}</span>
            }
            <div class="min-w-0">
              <h1 class="text-3xl md:text-4xl font-bold text-brand-700 leading-tight">{{ tienda.razonSocial }}</h1>
              <dl class="mt-3 space-y-1.5 text-gray-700">
                @if (tienda.direccion || tienda.nombreDistrito) {
                  <div class="flex items-start gap-2">
                    <dt class="sr-only">Dirección</dt>
                    <app-icon nombre="pin" [tamano]="18" class="text-gray-500 mt-0.5" />
                    <dd>{{ direccionConDistrito(tienda.direccion, tienda.nombreDistrito) }}</dd>
                  </div>
                }
                @if (tienda.horario) {
                  <div class="flex items-start gap-2">
                    <dt class="sr-only">Horario</dt>
                    <app-icon nombre="clock" [tamano]="18" class="text-gray-500 mt-0.5" />
                    <dd>{{ tienda.horario }}</dd>
                  </div>
                }
              </dl>
              @if (conUbicacion) {
                <a class="mt-3 inline-flex items-center gap-1.5 text-sm font-semibold text-brand-600 hover:underline" target="_blank" rel="noopener"
                   [href]="'https://www.google.com/maps/search/?api=1&query=' + tienda.latitud + ',' + tienda.longitud">
                  Cómo llegar en Google Maps <app-icon nombre="arrowRight" [tamano]="16" />
                </a>
              }
            </div>
          </div>
          @if (conMapa) {
            <div class="rounded-card overflow-hidden border border-surface-border">
              <app-mapa [lat]="tienda.latitud!" [lng]="tienda.longitud!" [alto]="200" />
            </div>
          }
        </div>
      </section>

      <section class="max-w-7xl mx-auto px-4 pt-8 pb-4">
        <h2 class="text-2xl font-bold text-brand-700">
          Por rescatar en esta tienda <span class="font-sans text-base font-normal text-gray-500 cifra">({{ productos().length }})</span>
        </h2>
        @if (productos().length) {
          <div class="mt-6 grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3 sm:gap-5">
            @for (producto of productos(); track producto.id) {
              <app-product-card [producto]="producto" />
            }
          </div>
        } @else {
          <div class="mt-6 text-center py-14 bg-surface-card border border-dashed border-surface-border rounded-card">
            <p class="text-gray-700">Esta tienda no tiene frutas por vencer en este momento.</p>
            <a routerLink="/productos" class="btn-secondary mt-4">Ver el resto del catálogo</a>
          </div>
        }
      </section>
    }
  `
})
export class TiendaDetalleComponent implements OnInit {
  private readonly catalogoService = inject(CatalogoService);
  readonly mapasDisponibles = inject(GoogleMapsLoaderService).configurado;

  readonly id = input.required<string>();
  readonly vendedor = signal<Vendedor | null>(null);
  readonly productos = signal<Producto[]>([]);
  readonly loading = signal(true);
  readonly tieneImagen = tieneImagen;
  readonly iniciales = iniciales;
  readonly direccionConDistrito = direccionConDistrito;

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
