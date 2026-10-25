export interface CarritoItem {
  productoId: number;
  nombre: string;
  precio: number;
  precioOriginal?: number;
  cantidad: number;
  imagen: string;
  vendedorId?: number;
  nombreVendedor?: string;
}