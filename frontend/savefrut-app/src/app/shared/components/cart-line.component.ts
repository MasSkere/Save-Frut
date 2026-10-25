import { ChangeDetectionStrategy, Component, computed, inject, input } from '@angular/core';
import { CarritoService } from '../../core/services/carrito.service';
import { CarritoItem } from '../models/carrito.model';
import { CurrencyPENPipe } from '../pipes/currency-pen.pipe';
import { CLASES_URGENCIA, diasHasta, etiquetaUrgencia, nivelUrgencia } from '../utils/producto.utils';
import { IconComponent } from './icon.component';
import { ProductImageComponent } from './product-image.component';

/**
 * Una línea del carrito: muestra qué se rescata (vencimiento y precio tachado) en el cajón,
 * en la página del carrito y en el resumen del checkout. En modo editable cambia cantidades.
 */
@Component({
  selector: 'app-cart-line',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CurrencyPENPipe, IconComponent, ProductImageComponent],
  template: `
    @let i = item();
    <div class="flex gap-3">
      <app-product-image class="relative w-16 h-16 rounded-lg shrink-0" [url]="i.imagen" [nombre]="i.nombre" [categoria]="i.categoria ?? ''"
                         [compacto]="true" [decorativo]="true" />
      <div class="flex-1 min-w-0">
        <div class="flex items-start justify-between gap-3">
          <p class="font-medium text-gray-900 leading-snug line-clamp-2">{{ i.nombre }}</p>
          <p class="font-semibold text-gray-900 cifra shrink-0">{{ i.precio * i.cantidad | currencyPEN }}</p>
        </div>
        <div class="mt-1 flex flex-wrap items-center gap-x-2 gap-y-1 text-xs text-gray-600 cifra">
          @if (dias() !== null) {
            <span class="font-semibold px-1.5 py-0.5 rounded" [class]="claseUrgencia()">{{ etiqueta() }}</span>
          }
          <span>
            @if (!editable()) { {{ i.cantidad }} × }{{ i.precio | currencyPEN }}
            @if (i.precioOriginal && i.precioOriginal > i.precio) {
              <s class="text-gray-400 ml-0.5">{{ i.precioOriginal | currencyPEN }}</s>
            }
          </span>
        </div>

        @if (editable()) {
          <div class="mt-2 flex items-center justify-between">
            <div class="flex items-center border border-surface-border rounded-full bg-white" role="group" [attr.aria-label]="'Cantidad de ' + i.nombre">
              <button type="button" class="w-8 h-8 flex items-center justify-center text-gray-700 hover:text-brand-700 disabled:text-gray-300"
                      [disabled]="i.cantidad <= 1" (click)="cambiar(i.cantidad - 1)" aria-label="Quitar uno"><app-icon nombre="minus" [tamano]="15" /></button>
              <span class="w-6 text-center text-sm font-semibold cifra">{{ i.cantidad }}</span>
              <button type="button" class="w-8 h-8 flex items-center justify-center text-gray-700 hover:text-brand-700"
                      (click)="cambiar(i.cantidad + 1)" aria-label="Agregar uno"><app-icon nombre="plus" [tamano]="15" /></button>
            </div>
            <button type="button" (click)="quitar()" class="inline-flex items-center gap-1 text-xs font-medium text-gray-500 hover:text-red-700 px-2 py-1 rounded"
                    [attr.aria-label]="'Quitar ' + i.nombre + ' del carrito'">
              <app-icon nombre="trash" [tamano]="14" />Quitar
            </button>
          </div>
        }
      </div>
    </div>
  `
})
export class CartLineComponent {
  private readonly carritoService = inject(CarritoService);

  readonly item = input.required<CarritoItem>();
  readonly editable = input(false);

  readonly dias = computed(() => {
    const fecha = this.item().fechaVencimiento;
    return fecha ? diasHasta(fecha) : null;
  });
  readonly etiqueta = computed(() => etiquetaUrgencia(this.dias() ?? 0));
  readonly claseUrgencia = computed(() => CLASES_URGENCIA[nivelUrgencia(this.dias() ?? 0)]);

  cambiar(cantidad: number): void {
    this.carritoService.actualizarCantidad(this.item().productoId, cantidad);
  }

  quitar(): void {
    this.carritoService.eliminar(this.item().productoId);
  }
}
