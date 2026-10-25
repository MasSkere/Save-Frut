import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../core/services/admin.service';
import { mensajeError } from '../../core/services/auth.service';
import { ProductoAdmin } from '../../shared/models/panel.model';
import { CurrencyPENPipe } from '../../shared/pipes/currency-pen.pipe';

@Component({
  selector: 'app-admin-productos',
  standalone: true,
  imports: [FormsModule, DatePipe, CurrencyPENPipe],
  template: `
    <div class="flex flex-wrap items-center justify-between gap-3 mb-6">
      <h1 class="text-2xl font-bold text-gray-900">Productos de la plataforma</h1>
      <input type="search" class="input-sf !w-64" placeholder="Filtrar por producto o tienda" aria-label="Filtrar productos"
             [ngModel]="filtro()" (ngModelChange)="filtro.set($event)" />
    </div>
    @if (error()) {
      <div class="mb-4 bg-red-50 border border-red-200 rounded-card p-3 text-sm text-red-700" role="alert">{{ error() }}</div>
    }
    <div class="card-sf overflow-x-auto">
      <table class="tabla-sf">
        <thead><tr><th>#</th><th>Producto</th><th>Tienda</th><th>Categoría</th><th class="text-right">Precio</th><th>Stock</th><th>Vence</th><th></th></tr></thead>
        <tbody>
          @for (p of filtrados(); track p.id) {
            <tr>
              <td>{{ p.id }}</td>
              <td class="font-medium">{{ p.nombre }}</td>
              <td>{{ p.nombreVendedor }}</td>
              <td>{{ p.nombreCategoria }}</td>
              <td class="text-right tabular-nums">{{ p.precioNormal | currencyPEN }}</td>
              <td [class.text-amber-600]="p.stock < 10" class="tabular-nums">{{ p.stock }}</td>
              <td class="whitespace-nowrap" [class.text-red-600]="vencido(p)">{{ p.fechaVencimiento | date: 'dd/MM/yyyy' }}</td>
              <td><button type="button" class="text-red-600 text-xs hover:underline" (click)="darDeBaja(p)">Dar de baja</button></td>
            </tr>
          } @empty {
            <tr><td colspan="8" class="text-center text-gray-500 py-8">{{ loading() ? 'Cargando…' : 'Sin productos.' }}</td></tr>
          }
        </tbody>
      </table>
    </div>
  `
})
export class AdminProductosComponent implements OnInit {
  private readonly service = inject(AdminService);
  readonly productos = signal<ProductoAdmin[]>([]);
  readonly filtro = signal('');
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  readonly filtrados = computed(() => {
    const q = this.filtro().toLowerCase();
    return this.productos().filter(p => !q || p.nombre.toLowerCase().includes(q) || (p.nombreVendedor ?? '').toLowerCase().includes(q));
  });

  ngOnInit(): void {
    this.service.getProductos().subscribe({
      next: p => { this.productos.set(p); this.loading.set(false); },
      error: err => { this.error.set(mensajeError(err)); this.loading.set(false); }
    });
  }

  vencido(p: ProductoAdmin): boolean {
    return new Date(p.fechaVencimiento + 'T23:59:59') < new Date();
  }

  darDeBaja(p: ProductoAdmin): void {
    this.service.darDeBajaProducto(p.id).subscribe({
      next: () => this.productos.update(l => l.filter(x => x.id !== p.id)),
      error: err => this.error.set(mensajeError(err))
    });
  }
}
