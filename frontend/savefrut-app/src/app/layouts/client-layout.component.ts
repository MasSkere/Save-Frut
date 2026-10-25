import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { CarritoService } from '../core/services/carrito.service';
import { AuthService } from '../core/services/auth.service';
import { CartDrawerComponent } from '../shared/components/cart-drawer/cart-drawer.component';

@Component({
  selector: 'app-client-layout',
  standalone: true,
  imports: [FormsModule, RouterOutlet, RouterLink, RouterLinkActive, CartDrawerComponent],
  templateUrl: './client-layout.component.html'
})
export class ClientLayoutComponent {
  private readonly carritoService = inject(CarritoService);
  private readonly router = inject(Router);
  readonly auth = inject(AuthService);

  readonly cartCount = this.carritoService.totalItems;
  readonly menuAbierto = signal(false);
  termino = '';

  openCartDrawer(): void {
    this.carritoService.openDrawer();
  }

  buscar(): void {
    const q = this.termino.trim();
    this.router.navigate(['/productos'], { queryParams: q ? { q } : {} });
  }

  salir(): void {
    this.menuAbierto.set(false);
    this.auth.logout();
  }
}
