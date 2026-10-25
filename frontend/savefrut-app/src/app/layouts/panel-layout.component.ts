import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/services/auth.service';

interface EnlacePanel {
  ruta: string;
  texto: string;
  exacto?: boolean;
}

const MENU_ADMIN: EnlacePanel[] = [
  { ruta: '/admin', texto: 'Dashboard', exacto: true },
  { ruta: '/admin/ordenes', texto: 'Órdenes' },
  { ruta: '/admin/productos', texto: 'Productos' },
  { ruta: '/admin/vendedores', texto: 'Vendedores' },
  { ruta: '/admin/usuarios', texto: 'Usuarios' }
];

const MENU_VENDEDOR: EnlacePanel[] = [
  { ruta: '/vendedor', texto: 'Dashboard', exacto: true },
  { ruta: '/vendedor/productos', texto: 'Mis productos' },
  { ruta: '/vendedor/ordenes', texto: 'Pedidos' },
  { ruta: '/vendedor/perfil', texto: 'Mi tienda' }
];

/** Layout con barra lateral para los paneles de administrador y vendedor. */
@Component({
  selector: 'app-panel-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="min-h-screen bg-surface-canvas md:flex">
      <aside class="bg-brand-900 text-white md:w-60 md:min-h-screen shrink-0 flex md:flex-col">
        <a routerLink="/" class="flex items-center gap-2 px-5 py-4 border-b border-brand-800">
          <span class="text-2xl" aria-hidden="true">🍊</span>
          <span class="font-bold">SaveFrut</span>
        </a>
        <p class="hidden md:block px-5 pt-4 pb-2 text-xs uppercase tracking-wide text-brand-300">
          {{ auth.isAdmin() ? 'Administración' : 'Vendedor' }}
        </p>
        <nav class="flex md:flex-col overflow-x-auto px-2 md:px-3 gap-1 py-2 flex-1">
          @for (e of menu(); track e.ruta) {
            <a [routerLink]="e.ruta" routerLinkActive="bg-brand-700 text-white" [routerLinkActiveOptions]="{ exact: !!e.exacto }"
               class="px-3 py-2 rounded-button text-sm text-brand-100 hover:bg-brand-800 whitespace-nowrap">{{ e.texto }}</a>
          }
        </nav>
        <div class="hidden md:block px-5 py-4 border-t border-brand-800 text-sm">
          <p class="font-medium truncate">{{ auth.user()?.nombre }}</p>
          <p class="text-brand-300 text-xs truncate">{{ auth.user()?.email }}</p>
          <button type="button" (click)="auth.logout()" class="mt-3 text-brand-200 hover:text-white text-xs underline">Cerrar sesión</button>
        </div>
      </aside>
      <main class="flex-1 min-w-0 p-4 md:p-8">
        <router-outlet></router-outlet>
      </main>
    </div>
  `
})
export class PanelLayoutComponent {
  readonly auth = inject(AuthService);
  readonly menu = computed(() => this.auth.isAdmin() ? MENU_ADMIN : MENU_VENDEDOR);
}
