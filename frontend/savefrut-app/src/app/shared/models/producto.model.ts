export interface Producto {
  id: number;
  nombre: string;
  descripcion: string;
  precioNormal: number;
  precioDescuento: number;
  fechaVencimiento: string;
  diasParaVencer: number;
  imagenUrl: string;
  nombreMedicion: string;
  nombreCategoria: string;
  nombreVendedor: string;
  vendedorId: number;
}

export interface Categoria {
  id: number;
  nombre: string;
  iconoUrl: string;
}

export interface TipoMedicion {
  id: number;
  nombre: string;
}

export interface Vendedor {
  id: number;
  razonSocial: string;
  logoUrl?: string;
  direccion?: string;
  horario?: string;
  latitud?: number;
  longitud?: number;
  nombreDistrito?: string;
}

export interface Ubicacion {
  id: number;
  nombre: string;
}

export type UrgenciaLevel = 'low' | 'medium' | 'high' | 'critical';
