import { Routes } from '@angular/router';
import { ClientLayoutComponent } from './layouts/client-layout.component';
import { PanelLayoutComponent } from './layouts/panel-layout.component';
import { HomeComponent } from './features/client/home/home.component';
import { adminGuard, clienteGuard, vendedorGuard } from './core/guards/role.guard';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent) },
  { path: 'registro', loadComponent: () => import('./features/auth/registro/registro.component').then(m => m.RegistroComponent) },
  {
    path: '',
    component: ClientLayoutComponent,
    children: [
      { path: '', component: HomeComponent, title: 'SaveFrut · Salva frutas, ahorra dinero' },
      { path: 'productos', title: 'Catálogo · SaveFrut', loadComponent: () => import('./features/client/productos/productos.component').then(m => m.ProductosComponent) },
      { path: 'productos/:id', loadComponent: () => import('./features/client/producto-detalle/producto-detalle.component').then(m => m.ProductoDetalleComponent) },
      { path: 'tiendas', title: 'Tiendas · SaveFrut', loadComponent: () => import('./features/client/tiendas/tiendas.component').then(m => m.TiendasComponent) },
      { path: 'tienda/:id', loadComponent: () => import('./features/client/tienda-detalle/tienda-detalle.component').then(m => m.TiendaDetalleComponent) },
      { path: 'carrito', loadComponent: () => import('./features/client/carrito/carrito.component').then(m => m.CarritoComponent) },
      { path: 'checkout', canActivate: [clienteGuard], title: 'Finalizar compra · SaveFrut', loadComponent: () => import('./features/client/checkout/checkout.component').then(m => m.CheckoutComponent) },
      { path: 'checkout/resultado', canActivate: [clienteGuard], loadComponent: () => import('./features/client/checkout/checkout-resultado.component').then(m => m.CheckoutResultadoComponent) },
      { path: 'mis-pedidos', canActivate: [clienteGuard], title: 'Mis pedidos · SaveFrut', loadComponent: () => import('./features/client/mis-pedidos/mis-pedidos.component').then(m => m.MisPedidosComponent) },
      { path: 'mis-puntos', canActivate: [clienteGuard], title: 'Eco-Puntos · SaveFrut', loadComponent: () => import('./features/client/mis-puntos/mis-puntos.component').then(m => m.MisPuntosComponent) }
    ]
  },
  {
    path: 'vendedor',
    component: PanelLayoutComponent,
    canActivate: [vendedorGuard],
    children: [
      { path: '', title: 'Panel vendedor · SaveFrut', loadComponent: () => import('./features/vendedor/vendedor-dashboard.component').then(m => m.VendedorDashboardComponent) },
      { path: 'productos', loadComponent: () => import('./features/vendedor/vendedor-productos.component').then(m => m.VendedorProductosComponent) },
      { path: 'ordenes', loadComponent: () => import('./features/vendedor/vendedor-ordenes.component').then(m => m.VendedorOrdenesComponent) },
      { path: 'perfil', loadComponent: () => import('./features/vendedor/vendedor-perfil.component').then(m => m.VendedorPerfilComponent) }
    ]
  },
  {
    path: 'admin',
    component: PanelLayoutComponent,
    canActivate: [adminGuard],
    children: [
      { path: '', title: 'Administración · SaveFrut', loadComponent: () => import('./features/admin/admin-dashboard.component').then(m => m.AdminDashboardComponent) },
      { path: 'ordenes', loadComponent: () => import('./features/admin/admin-ordenes.component').then(m => m.AdminOrdenesComponent) },
      { path: 'productos', loadComponent: () => import('./features/admin/admin-productos.component').then(m => m.AdminProductosComponent) },
      { path: 'vendedores', loadComponent: () => import('./features/admin/admin-vendedores.component').then(m => m.AdminVendedoresComponent) },
      { path: 'usuarios', loadComponent: () => import('./features/admin/admin-usuarios.component').then(m => m.AdminUsuariosComponent) }
    ]
  },
  { path: '**', redirectTo: '' }
];
