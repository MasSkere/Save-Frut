import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CarritoService, TARIFA_VARIEDAD } from '../../../core/services/carrito.service';
import { AuthService } from '../../../core/services/auth.service';
import { CurrencyPENPipe } from '../../../shared/pipes/currency-pen.pipe';
import { CartLineComponent } from '../../../shared/components/cart-line.component';
import { IconComponent } from '../../../shared/components/icon.component';

@Component({
  selector: 'app-carrito',
  standalone: true,
  imports: [RouterLink, CurrencyPENPipe, CartLineComponent, IconComponent],
  templateUrl: './carrito.component.html',
  styleUrl: './carrito.component.css'
})
export class CarritoComponent {
  private readonly carritoService = inject(CarritoService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly porTienda = this.carritoService.porTienda;
  readonly isEmpty = this.carritoService.isEmpty;
  readonly totalPrecio = this.carritoService.totalPrecio;
  readonly totalAhorro = this.carritoService.totalAhorro;
  readonly cantidadTiendas = this.carritoService.cantidadTiendas;
  readonly tarifaVariedad = TARIFA_VARIEDAD;

  irACheckout(): void {
    if (!this.authService.isCliente()) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: '/checkout' } });
      return;
    }
    this.router.navigate(['/checkout']);
  }
}
