import { ChangeDetectionStrategy, Component, computed, input, signal } from '@angular/core';
import { tieneImagen, tinteCategoria } from '../utils/producto.utils';

/**
 * Foto del producto. Si el vendedor aún no subió una, muestra un cartel de puesto
 * con el nombre sobre el tinte de su categoría, en lugar de un recuadro gris.
 */
@Component({
  selector: 'app-product-image',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'block overflow-hidden' },
  template: `
    @if (mostrarFoto()) {
      <img [src]="url()" [alt]="decorativo() ? '' : nombre()" class="w-full h-full object-cover" loading="lazy" (error)="fallo.set(true)" />
    } @else {
      <div class="w-full h-full flex flex-col justify-end p-[8%]" [class]="tinte()"
           [attr.role]="decorativo() ? null : 'img'" [attr.aria-label]="decorativo() ? null : nombre() + ', foto no disponible'"
           [attr.aria-hidden]="decorativo() ? 'true' : null">
        <span class="font-display font-bold text-brand-700 leading-[1.05] tracking-tight line-clamp-2"
              [class]="compacto() ? 'text-xs' : grande() ? 'text-4xl md:text-5xl' : 'text-xl md:text-2xl'">{{ nombre() }}</span>
        @if (!compacto()) {
          <span class="mt-1.5 text-xs font-medium text-brand-700/60">{{ categoria() }}</span>
        }
      </div>
    }
  `
})
export class ProductImageComponent {
  readonly url = input<string | null | undefined>();
  readonly nombre = input.required<string>();
  readonly categoria = input<string>('');
  /** Miniaturas (carrito, resumen): solo el nombre, pequeño. */
  readonly compacto = input(false);
  /** Ficha de detalle: cartel a gran tamaño. */
  readonly grande = input(false);
  /** Cuando el nombre ya está escrito al lado (tarjeta, carrito), el cartel no se anuncia dos veces. */
  readonly decorativo = input(false);

  readonly fallo = signal(false);
  readonly mostrarFoto = computed(() => tieneImagen(this.url()) && !this.fallo());
  readonly tinte = computed(() => tinteCategoria(this.categoria()));
}
