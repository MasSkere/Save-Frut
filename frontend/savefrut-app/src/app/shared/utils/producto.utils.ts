import { Producto, UrgenciaLevel } from '../models/producto.model';

export function porcentajeDescuento(p: Pick<Producto, 'precioNormal' | 'precioDescuento'>): number {
  if (!p.precioNormal) return 0;
  return Math.round(((p.precioNormal - p.precioDescuento) / p.precioNormal) * 100);
}

export function nivelUrgencia(dias: number): UrgenciaLevel {
  if (dias <= 0) return 'critical';
  if (dias <= 1) return 'high';
  if (dias <= 3) return 'medium';
  return 'low';
}

/** Días que faltan para una fecha 'yyyy-MM-dd', contados desde hoy en hora local (0 = vence hoy). */
export function diasHasta(fecha: string): number {
  const [a, m, d] = fecha.slice(0, 10).split('-').map(Number);
  const hoy = new Date();
  const inicioHoy = Date.UTC(hoy.getFullYear(), hoy.getMonth(), hoy.getDate());
  return Math.round((Date.UTC(a, m - 1, d) - inicioHoy) / 86_400_000);
}

export function etiquetaUrgencia(dias: number): string {
  if (dias < 0) return 'Vencido';
  if (dias === 0) return '¡Vence hoy!';
  if (dias === 1) return 'Vence mañana';
  return `Vence en ${dias} días`;
}

export const CLASES_URGENCIA: Record<UrgenciaLevel, string> = {
  low: 'bg-urgency-low text-amber-800',
  medium: 'bg-urgency-medium text-amber-900',
  high: 'bg-urgency-high text-white',
  critical: 'bg-urgency-critical text-white'
};

export const IMAGEN_TIENDA = 'https://placehold.co/200x200/deeede/2d5a27?text=Tienda';

/** El backend devuelve un placehold.co cuando el vendedor no subió foto; lo tratamos como "sin foto". */
export function tieneImagen(url: string | null | undefined): boolean {
  return !!url && !url.includes('placehold.co');
}

const TINTES_CATEGORIA: Record<string, string> = {
  acidas: 'bg-cat-acidas',
  semidulces: 'bg-cat-semidulces',
  dulces: 'bg-cat-dulces',
  neutras: 'bg-cat-neutras'
};

/** Fondo de cartel según la categoría ("Ácidas" → bg-cat-acidas). */
export function tinteCategoria(nombre: string | null | undefined): string {
  const clave = (nombre ?? '').normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase();
  return TINTES_CATEGORIA[clave] ?? 'bg-brand-50';
}

/** Dirección de una tienda con su distrito, sin repetirlo si la dirección ya lo trae. */
export function direccionConDistrito(direccion?: string | null, distrito?: string | null): string {
  const d = (direccion ?? '').trim();
  if (!distrito || d.toLowerCase().includes(distrito.toLowerCase())) return d;
  return d ? `${d}, ${distrito}` : distrito;
}

/** Iniciales para el sello de una tienda sin logo ("Bodega Don Pepe" → "BD"). */
export function iniciales(nombre: string | null | undefined): string {
  const palabras = (nombre ?? '').replace(/["“”']/g, '').split(/\s+/).filter(Boolean);
  return palabras.slice(0, 2).map(p => p[0]).join('').toUpperCase() || '?';
}
