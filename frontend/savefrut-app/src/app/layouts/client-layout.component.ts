import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { CarritoService } from '../core/services/carrito.service';
import { AuthService } from '../core/services/auth.service';
import { CartDrawerComponent } from '../shared/components/cart-drawer/cart-drawer.component';
import { IconComponent } from '../shared/components/icon.component';
import { LogoComponent } from '../shared/components/logo.component';

@Component({
  selector: 'app-client-layout',
  standalone: true,
  imports: [FormsModule, RouterOutlet, RouterLink, RouterLinkActive, CartDrawerComponent, IconComponent, LogoComponent],
  templateUrl: './client-layout.component.html',
  styles: `
    .nav-enlace {
      position: relative;
      padding: 0.5rem 0.75rem;
      font-size: 0.9rem;
      font-weight: 500;
      color: var(--color-gray-700);
      border-radius: var(--radius-button);
      transition: color 150ms ease;
    }
    .nav-enlace:hover {
      color: var(--color-brand-700);
    }
    .nav-activo::after {
      content: '';
      position: absolute;
      left: 0.75rem;
      right: 0.75rem;
      bottom: -0.6rem;
      height: 2px;
      border-radius: 2px;
      background: var(--color-brand-500);
    }
    .menu {
      transform-origin: top right;
      transition: opacity 150ms var(--ease-out), transform 150ms var(--ease-out);
    }
    @starting-style {
      .menu {
        opacity: 0;
        transform: scale(0.97) translateY(-4px);
      }
    }
    .menu-item {
      display: flex;
      align-items: center;
      gap: 0.625rem;
      padding: 0.5rem 0.625rem;
      font-size: 0.875rem;
      color: var(--color-gray-700);
      border-radius: 0.375rem;
      text-align: left;
    }
    .menu-item:hover {
      background: var(--color-brand-50);
      color: var(--color-brand-700);
    }
  `
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
