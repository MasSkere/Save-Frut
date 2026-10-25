import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CatalogoService } from '../../../core/services/catalogo.service';
import { CarritoService } from '../../../core/services/carrito.service';
import { Producto } from '../../../shared/models/producto.model';
import { CurrencyPENPipe } from '../../../shared/pipes/currency-pen.pipe';
import { CLASES_URGENCIA, etiquetaUrgencia, nivelUrgencia, porcentajeDescuento } from '../../../shared/utils/producto.utils';

@Component({
  selector: 'app-producto-detalle',
  standalone: true,
  imports: [RouterLink, CurrencyPENPipe, DatePipe],
  template: `
    @if (loading()) {
      <div class="flex justify-center py-20"><div class="animate-spin rounded-full h-12 w-12 border-b-2 border-brand-500"></div></div>
    } @else if (!producto()) {
      <div class="max-w-7xl mx-auto px-4 py-12 text-center">
        <p class="text-red-600">Este producto ya no está disponible (se agotó o venció).</p>
        <a routerLink="/productos" class="text-brand-600 underline">Volver al catálogo</a>
      </div>
    } @else {
      @let p = producto()!;
      <div class="max-w-6xl mx-auto px-4 py-6">
        <nav class="text-sm text-gray-500 mb-4">
          <a routerLink="/" class="hover:text-brand-500">Inicio</a> /
          <a routerLink="/productos" class="hover:text-brand-500">Catálogo</a> /
          <span class="text-gray-900">{{ p.nombre }}</span>
        </nav>
        <div class="grid md:grid-cols-2 gap-8">
          <div class="relative aspect-square rounded-card overflow-hidden bg-gray-100 shadow-soft">
            <img [src]="p.imagenUrl" [alt]="p.nombre" class="w-full h-full object-cover" />
            <span class="absolute top-4 left-4 bg-brand-500 text-white text-sm font-bold px-3 py-1.5 rounded-button">-{{ descuento() }}%</span>
          </div>
          <div>
            <span class="text-sm text-brand-400 uppercase tracking-wide">{{ p.nombreCategoria }}</span>
            <h1 class="text-3xl font-bold text-gray-900 mt-1">{{ p.nombre }}</h1>
            <a [routerLink]="['/tienda', p.vendedorId]" class="text-sm text-brand-600 hover:underline">Vendido por {{ p.nombreVendedor }}</a>

            <div class="mt-5 flex items-baseline gap-3">
              <span class="text-4xl font-bold text-brand-600">{{ p.precioDescuento | currencyPEN }}</span>
              <span class="text-lg text-gray-400 line-through">{{ p.precioNormal | currencyPEN }}</span>
              <span class="text-sm text-gray-500">/ {{ p.nombreMedicion }}</span>
            </div>

            <div class="mt-4 flex items-center gap-3">
              <span class="text-sm font-semibold px-3 py-1 rounded-button" [class]="claseUrgencia()">{{ etiqueta() }}</span>
              <span class="text-sm text-gray-500">Fecha de vencimiento: {{ p.fechaVencimiento | date: 'dd/MM/yyyy' }}</span>
            </div>

            <p class="mt-5 text-gray-700">{{ p.descripcion }}</p>

            <div class="mt-4 p-3 bg-brand-50 rounded-button text-sm text-brand-700">
              🌱 Comprando este producto salvas {{ cantidad() }} {{ p.nombreMedicion }} de fruta y ganas
              <strong>+{{ cantidad() * 5 }} Eco-Puntos</strong> extra.
            </div>

            <div class="mt-6 flex items-center gap-4">
              <div class="flex items-center border border-surface-border rounded-button">
                <button type="button" class="px-4 py-2 text-lg" (click)="cantidad.set(cantidad() > 1 ? cantidad() - 1 : 1)" aria-label="Disminuir">−</button>
                <span class="w-10 text-center font-semibold tabular-nums">{{ cantidad() }}</span>
                <button type="button" class="px-4 py-2 text-lg" (click)="cantidad.set(cantidad() + 1)" aria-label="Aumentar">+</button>
              </div>
              <button type="button" class="btn-primary flex-1 !py-3 !text-base" (click)="agregar()">
                Agregar · {{ total() | currencyPEN }}
              </button>
            </div>
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
