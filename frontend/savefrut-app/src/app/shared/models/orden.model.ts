export type EstadoOrden = 'PENDIENTE_PAGO' | 'PAGADO' | 'PREPARADO' | 'ENTREGADO' | 'CANCELADO';

export const ESTADOS_ORDEN: EstadoOrden[] = ['PENDIENTE_PAGO', 'PAGADO', 'PREPARADO', 'ENTREGADO', 'CANCELADO'];

export const ETIQUETA_ESTADO: Record<EstadoOrden, string> = {
  PENDIENTE_PAGO: 'Pendiente de pago',
  PAGADO: 'Pagado',
  PREPARADO: 'Preparado',
  ENTREGADO: 'Entregado',
  CANCELADO: 'Cancelado'
};

export interface DetalleItem {
  productoId: number;
  nombreProducto: string;
  vendedorId: number;
  cantidad: number;
  precioUnitario: number;
  subtotalItem: number;
}

export interface Orden {
  id: number;
  clienteId: number;
  nombreCliente: string | null;
  fechaOrden: string;
  estadoOrden: EstadoOrden;
  subtotal: number;
  costoEnvio: number;
  tarifaVariedad: number;
  total: number;
  direccionEnvio: string;
  distritoEnvio: string;
  latitudEnvio: number | null;
  longitudEnvio: number | null;
  fechaPago: string | null;
  items: DetalleItem[];
}

export interface CrearOrdenRequest {
  direccionEnvio: string;
  distritoNombre: string;
  tipoEntrega: 'DOMICILIO' | 'RECOJO';
  latitud?: number;
  longitud?: number;
  items: { productoId: number; cantidad: number }[];
}

export interface PreferenciaPago {
  ordenId: number;
  preferenceId: string;
  initPoint: string;
  sandboxInitPoint: string;
}

export interface VentaDiaria {
  fecha: string;
  total: number;
  cantidad: number;
}
