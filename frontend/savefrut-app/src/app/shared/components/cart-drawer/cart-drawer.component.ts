import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { CarritoService } from '../../../core/services/carrito.service';
import { AuthService } from '../../../core/services/auth.service';
import { CurrencyPENPipe } from '../../pipes/currency-pen.pipe';

@Component({
  selector: 'app-cart-drawer',
  standalone: true,
  imports: [CommonModule, RouterLink, CurrencyPENPipe],
  templateUrl: './cart-drawer.component.html',
  styleUrl: './cart-drawer.component.css'
})
export class CartDrawerComponent {
  private readonly carritoService = inject(CarritoService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly isOpen = this.carritoService.drawerOpen;
  readonly items = this.carritoService.items;
  readonly totalPrecio = this.carritoService.totalPrecio;
  readonly totalAhorro = this.carritoService.totalAhorro;
  readonly totalItems = this.carritoService.totalItems;
  readonly isEmpty = this.carritoService.isEmpty;

  closeDrawer(): void {
    this.carritoService.closeDrawer();
  }

  incrementar(productoId: number, cantidadActual: number): void {
    this.carritoService.actualizarCantidad(productoId, cantidadActual + 1);
  }

  decrementar(productoId: number, cantidadActual: number): void {
    this.carritoService.actualizarCantidad(productoId, cantidadActual - 1);
  }

  eliminarItem(productoId: number): void {
    this.carritoService.eliminar(productoId);
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
