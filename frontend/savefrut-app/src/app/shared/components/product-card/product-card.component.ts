import { Component, computed, inject, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Producto } from '../../models/producto.model';
import { CurrencyPENPipe } from '../../pipes/currency-pen.pipe';
import { CarritoService } from '../../../core/services/carrito.service';
import { CLASES_URGENCIA, etiquetaUrgencia, nivelUrgencia, porcentajeDescuento } from '../../utils/producto.utils';
import { IconComponent } from '../icon.component';
import { ProductImageComponent } from '../product-image.component';

@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [RouterLink, CurrencyPENPipe, IconComponent, ProductImageComponent],
  templateUrl: './product-card.component.html',
  styles: `
    .tarjeta {
      transition: box-shadow 250ms var(--ease-out), transform 250ms var(--ease-out);
    }
    @media (hover: hover) and (pointer: fine) {
      .tarjeta:hover {
        box-shadow: 0 10px 15px -3px rgb(0 0 0 / 0.1), 0 4px 6px -4px rgb(0 0 0 / 0.1);
        transform: translateY(-2px);
      }
    }
  `
})
export class ProductCardComponent {
  private readonly carritoService = inject(CarritoService);

  readonly producto = input.required<Producto>();

  readonly descuento = computed(() => porcentajeDescuento(this.producto()));
  readonly etiqueta = computed(() => etiquetaUrgencia(this.producto().diasParaVencer));
  readonly claseUrgencia = computed(() => CLASES_URGENCIA[nivelUrgencia(this.producto().diasParaVencer)]);

  agregar(): void {
    this.carritoService.agregarProducto(this.producto());
  }
}
