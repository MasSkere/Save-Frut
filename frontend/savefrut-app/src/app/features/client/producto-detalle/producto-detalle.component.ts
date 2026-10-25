import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CatalogoService } from '../../../core/services/catalogo.service';
import { CarritoService } from '../../../core/services/carrito.service';
import { Producto } from '../../../shared/models/producto.model';
import { CurrencyPENPipe } from '../../../shared/pipes/currency-pen.pipe';
import { CLASES_URGENCIA, etiquetaUrgencia, nivelUrgencia, porcentajeDescuento, tinteCategoria } from '../../../shared/utils/producto.utils';
import { IconComponent } from '../../../shared/components/icon.component';
import { ProductImageComponent } from '../../../shared/components/product-image.component';

@Component({
  selector: 'app-producto-detalle',
  standalone: true,
  imports: [RouterLink, CurrencyPENPipe, DatePipe, IconComponent, ProductImageComponent],
  template: `
    @if (loading()) {
      <div class="flex justify-center py-24" role="status">
        <div class="animate-spin rounded-full h-10 w-10 border-2 border-brand-100 border-t-brand-500" aria-hidden="true"></div>
        <span class="sr-only">Cargando producto…</span>
      </div>
    } @else if (!producto()) {
      <div class="max-w-xl mx-auto px-4 py-20 text-center">
        <p class="font-display text-2xl font-bold text-brand-700">Esta fruta ya no está disponible</p>
        <p class="mt-2 text-gray-600">Se agotó o ya venció. Hay otras esperando ser rescatadas.</p>
        <a routerLink="/productos" class="btn-primary mt-6">Ver el catálogo</a>
      </div>
    } @else {
      @let p = producto()!;
      <div class="max-w-6xl mx-auto px-4 pt-6 pb-4">
        <nav class="text-sm text-gray-600" aria-label="Ruta de navegación">
          <ol class="flex flex-wrap items-center gap-1.5">
            <li><a routerLink="/" class="hover:text-brand-700 hover:underline">Inicio</a></li>
            <li aria-hidden="true" class="text-gray-400">/</li>
            <li><a routerLink="/productos" class="hover:text-brand-700 hover:underline">Catálogo</a></li>
            <li aria-hidden="true" class="text-gray-400">/</li>
            <li class="text-gray-900 font-medium truncate max-w-[14rem]" aria-current="page">{{ p.nombre }}</li>
          </ol>
        </nav>

        <div class="mt-5 grid md:grid-cols-2 gap-8 lg:gap-14 items-start">
          <div class="relative aspect-square rounded-2xl overflow-hidden shadow-soft">
            <app-product-image class="absolute inset-0" [url]="p.imagenUrl" [nombre]="p.nombre" [categoria]="p.nombreCategoria" [grande]="true" />
            <span class="absolute top-4 left-4 text-sm font-semibold px-3 py-1.5 rounded-button" [class]="claseUrgencia()">{{ etiqueta() }}</span>
            <span class="absolute top-4 right-4 bg-brand-500 text-white text-sm font-bold px-3 py-1.5 rounded-button cifra">-{{ descuento() }}%</span>
          </div>

          <div class="md:pt-2">
            <span class="inline-flex items-center gap-2 text-sm font-medium text-gray-700">
              <span class="w-3 h-3 rounded-full ring-1 ring-black/5" [class]="tinte()" aria-hidden="true"></span>{{ p.nombreCategoria }}
            </span>
            <h1 class="mt-2 text-4xl lg:text-5xl font-bold text-brand-700 leading-[1.05]">{{ p.nombre }}</h1>
            <a [routerLink]="['/tienda', p.vendedorId]" class="mt-3 inline-flex items-center gap-2 text-brand-600 font-medium hover:underline">
              <app-icon nombre="store" [tamano]="18" />{{ p.nombreVendedor }}
            </a>

            <div class="mt-7 flex flex-wrap items-baseline gap-x-3 gap-y-1 cifra">
              <span class="font-display text-5xl font-bold text-brand-600 leading-none">{{ p.precioDescuento | currencyPEN }}</span>
              <span class="text-lg text-gray-500 line-through">{{ p.precioNormal | currencyPEN }}</span>
              <span class="text-gray-600">por {{ p.nombreMedicion }}</span>
            </div>
            <p class="mt-3 inline-flex items-center gap-2 text-sm text-gray-700">
              <app-icon nombre="clock" [tamano]="16" class="text-gray-500" />
              Vence el {{ p.fechaVencimiento | date: "EEEE d 'de' MMMM" : 'UTC' }}
            </p>

            @if (p.descripcion) {
              <p class="mt-6 text-gray-700 leading-relaxed max-w-prose">{{ p.descripcion }}</p>
            }

            <div class="mt-8 flex items-stretch gap-3">
              <div class="flex items-center border border-surface-border rounded-button bg-white" role="group" aria-label="Cantidad">
                <button type="button" class="w-11 h-12 flex items-center justify-center text-gray-700 hover:text-brand-700 disabled:text-gray-300" [disabled]="cantidad() <= 1"
                        (click)="cantidad.set(cantidad() - 1)" aria-label="Quitar uno"><app-icon nombre="minus" [tamano]="18" /></button>
                <span class="w-8 text-center font-semibold cifra" aria-live="polite">{{ cantidad() }}</span>
                <button type="button" class="w-11 h-12 flex items-center justify-center text-gray-700 hover:text-brand-700"
                        (click)="cantidad.set(cantidad() + 1)" aria-label="Agregar uno"><app-icon nombre="plus" [tamano]="18" /></button>
              </div>
              <button type="button" class="btn-primary flex-1 !text-base" (click)="agregar()">
                Agregar al carrito · <span class="cifra">{{ total() | currencyPEN }}</span>
              </button>
            </div>

            <p class="mt-4 text-sm text-gray-700 leading-relaxed">
              Con esta compra salvas <strong class="font-semibold text-brand-700">{{ cantidad() }} {{ p.nombreMedicion }}</strong> de fruta
              y ganas <strong class="font-semibold text-brand-700">+{{ cantidad() * 5 }} Eco-Puntos</strong> extra.
            </p>
          </div>
        </div>
      </div>
    }
  `
})
export class ProductoDetalleComponent implements OnInit {
  private readonly catalogoService = inject(CatalogoService);
  private readonly carritoService = inject(CarritoService);

  readonly id = input.required<string>();
  readonly producto = signal<Producto | null>(null);
  readonly loading = signal(true);
  readonly cantidad = signal(1);

  readonly total = computed(() => this.cantidad() * (this.producto()?.precioDescuento ?? 0));
  readonly descuento = computed(() => this.producto() ? porcentajeDescuento(this.producto()!) : 0);
  readonly etiqueta = computed(() => etiquetaUrgencia(this.producto()?.diasParaVencer ?? 0));
  readonly claseUrgencia = computed(() => CLASES_URGENCIA[nivelUrgencia(this.producto()?.diasParaVencer ?? 0)]);
  readonly tinte = computed(() => tinteCategoria(this.producto()?.nombreCategoria));

  ngOnInit(): void {
    this.catalogoService.getProducto(Number(this.id())).subscribe({
      next: p => { this.producto.set(p); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  agregar(): void {
    const p = this.producto();
    if (p) {
      this.carritoService.agregarProducto(p, this.cantidad());
    }
  }
}
