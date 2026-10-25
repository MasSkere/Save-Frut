import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/services/auth.service';
import { LogoComponent } from '../shared/components/logo.component';
import { IconComponent, NombreIcono } from '../shared/components/icon.component';

interface EnlacePanel {
  ruta: string;
  texto: string;
  icono: NombreIcono;
  exacto?: boolean;
}

const MENU_ADMIN: EnlacePanel[] = [
  { ruta: '/admin', texto: 'Dashboard', icono: 'dashboard', exacto: true },
  { ruta: '/admin/ordenes', texto: 'Órdenes', icono: 'package' },
  { ruta: '/admin/productos', texto: 'Productos', icono: 'basket' },
  { ruta: '/admin/vendedores', texto: 'Vendedores', icono: 'store' },
  { ruta: '/admin/usuarios', texto: 'Usuarios', icono: 'users' }
];

const MENU_VENDEDOR: EnlacePanel[] = [
  { ruta: '/vendedor', texto: 'Resumen', icono: 'dashboard', exacto: true },
  { ruta: '/vendedor/productos', texto: 'Mis productos', icono: 'basket' },
  { ruta: '/vendedor/ordenes', texto: 'Pedidos', icono: 'package' },
  { ruta: '/vendedor/perfil', texto: 'Mi tienda', icono: 'store' }
];

/** Layout con barra lateral para los paneles de administrador y vendedor. */
@Component({
  selector: 'app-panel-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, LogoComponent, IconComponent],
  template: `
    <div class="min-h-screen bg-surface-canvas md:flex">
      <aside class="bg-brand-900 text-white md:w-60 md:h-screen md:sticky md:top-0 shrink-0 flex flex-col">
        <div class="flex items-center justify-between md:block">
          <a routerLink="/" class="flex items-center px-5 h-16 md:border-b md:border-brand-800" aria-label="SaveFrut, ir al inicio">
            <app-logo [invertido]="true" [tamano]="28" />
          </a>
          <button type="button" (click)="auth.logout()" class="md:hidden mr-3 inline-flex items-center gap-1.5 px-3 py-2 text-sm text-brand-200 hover:text-white rounded-button">
            <app-icon nombre="logout" [tamano]="16" />Salir
          </button>
        </div>
        <p class="hidden md:block px-5 pt-5 pb-2 text-xs font-semibold text-brand-300">
          {{ auth.isAdmin() ? 'Administración' : 'Mi tienda' }}
        </p>
        <nav class="flex md:flex-col overflow-x-auto px-3 gap-1 pb-3 md:pb-0 md:flex-1 [scrollbar-width:none]" [attr.aria-label]="auth.isAdmin() ? 'Administración' : 'Panel de vendedor'">
          @for (e of menu(); track e.ruta) {
            <a [routerLink]="e.ruta" routerLinkActive="!bg-brand-700 !text-white" [routerLinkActiveOptions]="{ exact: !!e.exacto }"
               class="flex items-center gap-2.5 px-3 py-2 rounded-button text-sm text-brand-100/90 hover:bg-brand-800 hover:text-white whitespace-nowrap transition-colors">
              <app-icon [nombre]="e.icono" [tamano]="18" class="opacity-80" />{{ e.texto }}
            </a>
          }
        </nav>
        <div class="hidden md:block px-5 py-4 border-t border-brand-800 text-sm">
          <p class="font-medium truncate">{{ auth.user()?.nombre }}</p>
          <p class="text-brand-300 text-xs truncate">{{ auth.user()?.email }}</p>
          <button type="button" (click)="auth.logout()" class="mt-3 inline-flex items-center gap-1.5 text-brand-200 hover:text-white text-xs">
            <app-icon nombre="logout" [tamano]="14" />Cerrar sesión
          </button>
        </div>
      </aside>
      <main class="flex-1 min-w-0 p-4 md:p-8">
        <div class="max-w-6xl">
          <router-outlet></router-outlet>
        </div>
      </main>
    </div>
  `
})
export class PanelLayoutComponent {
  readonly auth = inject(AuthService);
  readonly menu = computed(() => this.auth.isAdmin() ? MENU_ADMIN : MENU_VENDEDOR);
}
