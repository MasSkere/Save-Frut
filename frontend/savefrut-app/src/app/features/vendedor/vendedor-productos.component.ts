import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { VendedorPanelService } from '../../core/services/vendedor-panel.service';
import { CatalogoService } from '../../core/services/catalogo.service';
import { mensajeError } from '../../core/services/auth.service';
import { ProductoRequest, ProductoVendedor } from '../../shared/models/panel.model';
import { Categoria, TipoMedicion } from '../../shared/models/producto.model';
import { CurrencyPENPipe } from '../../shared/pipes/currency-pen.pipe';

const VACIO: ProductoRequest = {
  nombre: '', descripcion: '', precioNormal: 0, stock: 0, fechaVencimiento: '', imagenUrl: '', categoriaId: 0, medicionId: 0
};

@Component({
  selector: 'app-vendedor-productos',
  standalone: true,
  imports: [FormsModule, DatePipe, CurrencyPENPipe],
  templateUrl: './vendedor-productos.component.html'
})
export class VendedorProductosComponent implements OnInit {
  private readonly service = inject(VendedorPanelService);
  private readonly catalogoService = inject(CatalogoService);

  readonly productos = signal<ProductoVendedor[]>([]);
  readonly categorias = signal<Categoria[]>([]);
  readonly medidas = signal<TipoMedicion[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly guardando = signal(false);
  readonly editandoId = signal<number | null>(null);
  readonly formAbierto = signal(false);

  form: ProductoRequest = { ...VACIO };

  ngOnInit(): void {
    forkJoin({ categorias: this.catalogoService.getCategorias(), medidas: this.catalogoService.getTiposMedicion() })
      .subscribe(r => {
        this.categorias.set(r.categorias);
        this.medidas.set(r.medidas);
      });
    this.cargar();
  }

  cargar(): void {
    this.service.getProductos().subscribe({
      next: p => { this.productos.set(p); this.loading.set(false); },
      error: err => { this.error.set(mensajeError(err)); this.loading.set(false); }
    });
  }

  nuevo(): void {
    this.form = { ...VACIO, categoriaId: this.categorias()[0]?.id ?? 0, medicionId: this.medidas()[0]?.id ?? 0 };
    this.editandoId.set(null);
    this.formAbierto.set(true);
  }

  editar(p: ProductoVendedor): void {
    this.form = {
      nombre: p.nombre, descripcion: p.descripcion, precioNormal: p.precioNormal, stock: p.stock,
      fechaVencimiento: p.fechaVencimiento, imagenUrl: p.imagenUrl, categoriaId: p.categoriaId, medicionId: p.medicionId
    };
    this.editandoId.set(p.id);
    this.formAbierto.set(true);
  }

  guardar(): void {
    this.guardando.set(true);
    this.error.set(null);
    const id = this.editandoId();
    const peticion = id ? this.service.actualizarProducto(id, this.form) : this.service.crearProducto(this.form);
    peticion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.formAbierto.set(false);
        this.cargar();
      },
      error: err => {
        this.guardando.set(false);
        this.error.set(mensajeError(err, 'No se pudo guardar el producto'));
      }
    });
  }

  ajustarStock(p: ProductoVendedor, valor: string): void {
    const stock = Number(valor);
    if (Number.isNaN(stock) || stock < 0 || stock === p.stock) return;
    this.service.ajustarStock(p.id, stock).subscribe({
      next: actualizado => this.reemplazar(actualizado),
      error: err => this.error.set(mensajeError(err))
    });
  }

  cambiarActivo(p: ProductoVendedor): void {
    this.service.cambiarActivo(p.id, !p.activo).subscribe({
      next: actualizado => this.reemplazar(actualizado),
      error: err => this.error.set(mensajeError(err))
    });
  }

  private reemplazar(actualizado: ProductoVendedor): void {
    this.productos.update(lista => lista.map(x => x.id === actualizado.id ? actualizado : x));
  }
}
