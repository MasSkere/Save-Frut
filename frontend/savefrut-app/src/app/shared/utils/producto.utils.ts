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
