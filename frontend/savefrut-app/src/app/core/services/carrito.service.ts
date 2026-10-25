import { Injectable, signal, computed, effect } from '@angular/core';
import { CarritoItem } from '../../shared/models/carrito.model';
import { Producto } from '../../shared/models/producto.model';

const CART_STORAGE_KEY = 'savefrut_cart';

/** Carrito local (localStorage). La orden real se crea en el checkout contra ms-ordenes. */
@Injectable({ providedIn: 'root' })
export class CarritoService {
  private readonly _items = signal<CarritoItem[]>(this.loadFromStorage());
  private readonly _drawerOpen = signal(false);

  readonly items = this._items.asReadonly();
  readonly drawerOpen = this._drawerOpen.asReadonly();

  readonly totalItems = computed(() => this._items().reduce((acc, i) => acc + i.cantidad, 0));
  readonly totalPrecio = computed(() => this._items().reduce((acc, i) => acc + i.precio * i.cantidad, 0));
  readonly totalAhorro = computed(() =>
    this._items().reduce((acc, i) => acc + ((i.precioOriginal ?? i.precio) - i.precio) * i.cantidad, 0));
  readonly isEmpty = computed(() => this._items().length === 0);
  /** Comprar a más de una tienda agrega la tarifa por variedad (S/ 5.00). */
  readonly cantidadTiendas = computed(() => new Set(this._items().map(i => i.vendedorId)).size);

  constructor() {
    effect(() => localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(this._items())));
  }

  agregarProducto(producto: Producto, cantidad = 1): void {
    const existente = this._items().find(i => i.productoId === producto.id);
    if (existente) {
      this.actualizarCantidad(producto.id, existente.cantidad + cantidad);
    } else {
      this._items.update(items => [...items, {
        productoId: producto.id,
        nombre: producto.nombre,
        precio: producto.precioDescuento,
        precioOriginal: producto.precioNormal,
        cantidad,
        imagen: producto.imagenUrl,
        vendedorId: producto.vendedorId,
        nombreVendedor: producto.nombreVendedor
      }]);
    }
    this._drawerOpen.set(true);
  }

  eliminar(productoId: number): void {
    this._items.update(items => items.filter(i => i.productoId !== productoId));
  }

  actualizarCantidad(productoId: number, cantidad: number): void {
    if (cantidad <= 0) {
      this.eliminar(productoId);
      return;
    }
    this._items.update(items => items.map(i => i.productoId === productoId ? { ...i, cantidad } : i));
  }

  vaciar(): void {
    this._items.set([]);
  }

  openDrawer(): void {
    this._drawerOpen.set(true);
  }

  closeDrawer(): void {
    this._drawerOpen.set(false);
  }

  private loadFromStorage(): CarritoItem[] {
    try {
      return JSON.parse(localStorage.getItem(CART_STORAGE_KEY) ?? '[]');
    } catch {
      return [];
    }
  }
}
