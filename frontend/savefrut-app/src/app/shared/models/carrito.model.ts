export interface CarritoItem {
  productoId: number;
  nombre: string;
  precio: number;
  precioOriginal?: number;
  cantidad: number;
  imagen: string;
  vendedorId?: number;
  nombreVendedor?: string;
  /** Opcionales: los carritos guardados antes de este cambio no los traen. */
  categoria?: string;
  fechaVencimiento?: string;
}
