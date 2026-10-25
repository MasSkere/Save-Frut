import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CarritoService } from '../../../core/services/carrito.service';
import { CurrencyPENPipe } from '../../../shared/pipes/currency-pen.pipe';

@Component({
  selector: 'app-carrito',
  standalone: true,
  imports: [CommonModule, RouterLink, CurrencyPENPipe],
  templateUrl: './carrito.component.html',
  styleUrl: './carrito.component.css'
})
export class CarritoComponent {
  private readonly carritoService = inject(CarritoService);

  readonly items = this.carritoService.items;
  readonly isEmpty = this.carritoService.isEmpty;
  readonly totalPrecio = this.carritoService.totalPrecio;
  readonly totalAhorro = this.carritoService.totalAhorro;

  incrementar(productoId: number, cantidadActual: number): void {
    this.carritoService.actualizarCantidad(productoId, cantidadActual + 1);
  }

  decrementar(productoId: number, cantidadActual: number): void {
    this.carritoService.actualizarCantidad(productoId, cantidadActual - 1);
  }

  eliminar(productoId: number): void {
    this.carritoService.eliminar(productoId);
  }

  irACheckout(): void {
    // Later will check auth and redirect to /checkout/envio
  }
}