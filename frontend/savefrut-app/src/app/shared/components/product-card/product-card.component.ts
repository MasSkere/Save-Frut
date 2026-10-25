import { Component, computed, inject, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Producto } from '../../models/producto.model';
import { CurrencyPENPipe } from '../../pipes/currency-pen.pipe';
import { CarritoService } from '../../../core/services/carrito.service';
import { CLASES_URGENCIA, etiquetaUrgencia, nivelUrgencia, porcentajeDescuento } from '../../utils/producto.utils';

@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [RouterLink, CurrencyPENPipe],
  templateUrl: './product-card.component.html'
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
