import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { CatalogoService } from '../../../core/services/catalogo.service';
import { Categoria, Producto, Vendedor } from '../../../shared/models/producto.model';
import { ProductCardComponent } from '../../../shared/components/product-card/product-card.component';
import { IMAGEN_TIENDA } from '../../../shared/utils/producto.utils';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, ProductCardComponent],
  templateUrl: './home.component.html'
})
export class HomeComponent implements OnInit {
  private readonly catalogoService = inject(CatalogoService);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly productos = signal<Producto[]>([]);
  readonly categorias = signal<Categoria[]>([]);
  readonly vendedores = signal<Vendedor[]>([]);
  readonly imagenTienda = IMAGEN_TIENDA;

  /** Los más urgentes primero: los que vencen hoy o mañana tienen 60% de descuento. */
  readonly urgentes = computed(() =>
    [...this.productos()].sort((a, b) => a.diasParaVencer - b.diasParaVencer).slice(0, 8));

  ngOnInit(): void {
    forkJoin({
      productos: this.catalogoService.getProductos(),
      categorias: this.catalogoService.getCategorias(),
      vendedores: this.catalogoService.getVendedores()
    }).subscribe({
      next: r => {
        this.productos.set(r.productos);
        this.categorias.set(r.categorias);
        this.vendedores.set(r.vendedores.slice(0, 8));
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar el catálogo. Verifica que el backend esté en ejecución.');
        this.loading.set(false);
      }
    });
  }
}
