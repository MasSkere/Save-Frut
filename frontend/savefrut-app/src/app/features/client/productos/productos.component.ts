import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { CatalogoService } from '../../../core/services/catalogo.service';
import { Categoria, Producto } from '../../../shared/models/producto.model';
import { ProductCardComponent } from '../../../shared/components/product-card/product-card.component';
import { tinteCategoria } from '../../../shared/utils/producto.utils';

type Orden = 'urgencia' | 'precio' | 'descuento';

@Component({
  selector: 'app-productos',
  standalone: true,
  imports: [FormsModule, ProductCardComponent],
  templateUrl: './productos.component.html',
  styles: `
    .chip {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      height: 2.5rem;
      padding: 0 1rem;
      border-radius: 9999px;
      border: 1px solid var(--color-surface-border);
      background: #fff;
      font-size: 0.875rem;
      font-weight: 500;
      color: var(--color-gray-700);
      white-space: nowrap;
      transition: background-color 150ms ease, border-color 150ms ease, color 150ms ease;
    }
    .chip:hover {
      border-color: var(--color-brand-300);
      color: var(--color-brand-700);
    }
    .chip-activo,
    .chip-activo:hover {
      background: var(--color-brand-700);
      border-color: var(--color-brand-700);
      color: #fff;
    }
  `
})
export class ProductosComponent implements OnInit {
  private readonly catalogoService = inject(CatalogoService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly productos = signal<Producto[]>([]);
  readonly categorias = signal<Categoria[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  readonly termino = signal('');
  readonly categoriaId = signal<number | null>(null);
  readonly orden = signal<Orden>('urgencia');
  readonly tinteCategoria = tinteCategoria;

  readonly filtrados = computed(() => {
    const q = this.termino().toLowerCase();
    const cat = this.categorias().find(c => c.id === this.categoriaId())?.nombre;
    const lista = this.productos().filter(p =>
      (!q || p.nombre.toLowerCase().includes(q) || p.nombreVendedor?.toLowerCase().includes(q)) &&
      (!cat || p.nombreCategoria === cat));
    const comparar: Record<Orden, (a: Producto, b: Producto) => number> = {
      urgencia: (a, b) => a.diasParaVencer - b.diasParaVencer,
      precio: (a, b) => a.precioDescuento - b.precioDescuento,
      descuento: (a, b) => (b.precioNormal - b.precioDescuento) / b.precioNormal - (a.precioNormal - a.precioDescuento) / a.precioNormal
    };
    return lista.sort(comparar[this.orden()]);
  });

  ngOnInit(): void {
    this.route.queryParamMap.subscribe(params => {
      this.termino.set(params.get('q') ?? '');
      const cat = params.get('categoria');
      this.categoriaId.set(cat ? Number(cat) : null);
    });

    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);
    forkJoin({ productos: this.catalogoService.getProductos(), categorias: this.catalogoService.getCategorias() })
      .subscribe({
        next: r => {
          this.productos.set(r.productos);
          this.categorias.set(r.categorias);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('No pudimos cargar el catálogo. Revisa tu conexión e inténtalo de nuevo.');
          this.loading.set(false);
        }
      });
  }

  limpiarBusqueda(): void {
    this.router.navigate([], { queryParams: { q: null }, queryParamsHandling: 'merge' });
  }

  verTodo(): void {
    this.router.navigate([], { queryParams: {} });
  }

  seleccionarCategoria(id: number | null): void {
    this.router.navigate([], {
      queryParams: { categoria: id, q: this.termino() || null },
      queryParamsHandling: 'merge'
    });
  }
}
