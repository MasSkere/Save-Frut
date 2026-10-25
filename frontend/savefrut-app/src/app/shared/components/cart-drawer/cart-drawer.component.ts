import { Component, HostListener, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CarritoService, TARIFA_VARIEDAD } from '../../../core/services/carrito.service';
import { AuthService } from '../../../core/services/auth.service';
import { CurrencyPENPipe } from '../../pipes/currency-pen.pipe';
import { CartLineComponent } from '../cart-line.component';
import { IconComponent } from '../icon.component';

@Component({
  selector: 'app-cart-drawer',
  standalone: true,
  imports: [RouterLink, CurrencyPENPipe, CartLineComponent, IconComponent],
  templateUrl: './cart-drawer.component.html',
  styleUrl: './cart-drawer.component.css'
})
export class CartDrawerComponent {
  private readonly carritoService = inject(CarritoService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly isOpen = this.carritoService.drawerOpen;
  readonly porTienda = this.carritoService.porTienda;
  readonly totalPrecio = this.carritoService.totalPrecio;
  readonly totalAhorro = this.carritoService.totalAhorro;
  readonly totalItems = this.carritoService.totalItems;
  readonly cantidadTiendas = this.carritoService.cantidadTiendas;
  readonly isEmpty = this.carritoService.isEmpty;
  readonly tarifaVariedad = TARIFA_VARIEDAD;

  @HostListener('document:keydown.escape')
  alPresionarEscape(): void {
    if (this.isOpen()) {
      this.closeDrawer();
    }
  }

  closeDrawer(): void {
    this.carritoService.closeDrawer();
  }

  finalizarCompra(): void {
    this.closeDrawer();
    if (!this.authService.isCliente()) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: '/checkout' } });
      return;
    }
    this.router.navigate(['/checkout']);
  }

  irAComprar(): void {
    this.closeDrawer();
  }
}
