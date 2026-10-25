import { DetalleItem, VentaDiaria } from './orden.model';

export interface ResumenProductos {
  productosActivos: number;
  productosStockBajo: number;
  productosPorVencer: number;
  productosVencidos: number;
}

export interface Alerta {
  tipo: 'STOCK_BAJO' | 'POR_VENCER' | 'VENCIDO';
  mensaje: string;
  productoId: number;
}

export interface ProductoVendedor {
  id: number;
  nombre: string;
  descripcion: string;
  precioNormal: number;
  precioDescuento: number;
  stock: number;
  fechaVencimiento: string;
  imagenUrl: string;
  nombreCategoria: string;
  activo: boolean;
  categoriaId: number;
  medicionId: number;
}

export interface ProductoRequest {
  nombre: string;
  descripcion: string;
  precioNormal: number;
  stock: number;
  fechaVencimiento: string;
  imagenUrl: string;
  categoriaId: number;
  medicionId: number;
  vendedorId?: number;
}

export interface ProductoAdmin {
  id: number;
  nombre: string;
  descripcion: string;
  precioNormal: number;
  stock: number;
  fechaVencimiento: string;
  imagenUrl: string;
  nombreCategoria: string;
  nombreVendedor: string;
  baja: boolean;
}

export interface OrdenVendedor {
  id: number;
  fechaOrden: string;
  estadoOrden: string;
  totalVendedor: number;
  nombreCliente: string | null;
  direccionEnvio: string;
  distritoEnvio: string;
  latitudEnvio: number | null;
  longitudEnvio: number | null;
  items: DetalleItem[];
}

export interface ResumenVendedor {
  ingresosHoy: number;
  ingresosSemana: number;
  ordenesPorPreparar: number;
  unidadesVendidas: number;
  ventasSemana: VentaDiaria[];
}

export interface PerfilVendedor {
  id: number;
  ruc: string;
  razonSocial: string;
  direccion: string;
  horario: string;
  latitud: number;
  longitud: number;
  nombreDistrito: string;
  logoUrl: string;
  aprobado: boolean;
}

export interface ProductoVendido {
  id: number;
  nombre: string;
  cantidadVendida: number;
  revenue: number;
}

export interface DashboardAdmin {
  ventasHoy: number;
  ventasMes: number;
  ordenesHoy: number;
  ordenesPorEstado: Record<string, number>;
  clientes: number;
  vendedoresAprobados: number;
  vendedoresPendientes: number;
  ventasSemana: VentaDiaria[];
  topProductos: ProductoVendido[];
}

export interface UsuarioAdmin {
  id: number;
  email: string;
  tipo: string;
  estado: string;
  fechaRegistro: string;
}

export interface VendedorAdmin {
  id: number;
  ruc: string;
  razonSocial: string;
  direccion: string;
  horario: string;
  latitud: number;
  longitud: number;
  nombreDistrito: string | null;
  aprobado: boolean;
  usuarioId: number;
  email: string;
}

export interface EstadisticasPuntos {
  clientesConCompras: number;
  clientesRecurrentes: number;
  tasaRetencion: number;
  frutasSalvadasTotal: number;
  ranking: { clienteId: number; puntos: number; frutasSalvadas: number; compras: number }[];
}

export interface MisPuntos {
  puntosAcumulados: number;
  montoTotalGastado: number;
  frutasSalvadas: number;
  totalCompras: number;
  nivel: string;
  puntosParaSiguienteNivel: number | null;
  movimientos: { ordenId: number; puntos: number; monto: number; frutasSalvadas: number; fecha: string }[];
}
