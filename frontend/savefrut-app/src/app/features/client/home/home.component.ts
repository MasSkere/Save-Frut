import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { CatalogoService } from '../../../core/services/catalogo.service';
import { Categoria, Producto, Vendedor } from '../../../shared/models/producto.model';
import { ProductCardComponent } from '../../../shared/components/product-card/product-card.component';
import { IconComponent } from '../../../shared/components/icon.component';
import { CurrencyPENPipe } from '../../../shared/pipes/currency-pen.pipe';
import { CLASES_URGENCIA, etiquetaUrgencia, iniciales, nivelUrgencia, tieneImagen, tinteCategoria } from '../../../shared/utils/producto.utils';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, ProductCardComponent, IconComponent, CurrencyPENPipe],
  templateUrl: './home.component.html'
})
export class HomeComponent implements OnInit {
  private readonly catalogoService = inject(CatalogoService);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly productos = signal<Producto[]>([]);
  readonly categorias = signal<Categoria[]>([]);
  readonly vendedores = signal<Vendedor[]>([]);

  /** Los más urgentes primero: los que vencen en 2 días o menos tienen 60% de descuento. */
  readonly ordenados = computed(() =>
    [...this.productos()].sort((a, b) => a.diasParaVencer - b.diasParaVencer));
  /** La pizarra del hero muestra los tres que vencen primero; la grilla sigue con los siguientes. */
  readonly pizarra = computed(() => this.ordenados().slice(0, 3));
  readonly resto = computed(() => this.ordenados().slice(3, 11));

  readonly tieneImagen = tieneImagen;
  readonly iniciales = iniciales;
  readonly tinteCategoria = tinteCategoria;
  readonly etiquetaUrgencia = etiquetaUrgencia;

  claseUrgencia(dias: number): string {
    return CLASES_URGENCIA[nivelUrgencia(dias)];
  }

  cantidadEnCategoria(nombre: string): number {
    return this.productos().filter(p => p.nombreCategoria === nombre).length;
  }

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);
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
        this.error.set('No pudimos cargar las frutas por rescatar. Revisa tu conexión e inténtalo de nuevo.');
        this.loading.set(false);
      }
    });
  }
}
