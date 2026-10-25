import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Alerta, OrdenVendedor, PerfilVendedor, ProductoRequest, ProductoVendedor, ResumenProductos, ResumenVendedor
} from '../../shared/models/panel.model';

@Injectable({ providedIn: 'root' })
export class VendedorPanelService {
  private readonly http = inject(HttpClient);
  private readonly api = environment.apiUrl;

  // Productos (ms-catalogo)
  getProductos(): Observable<ProductoVendedor[]> {
    return this.http.get<ProductoVendedor[]>(`${this.api}/vendedor/productos`, { params: { size: 200 } });
  }

  crearProducto(data: ProductoRequest): Observable<ProductoVendedor> {
    return this.http.post<ProductoVendedor>(`${this.api}/vendedor/productos`, data);
  }

  actualizarProducto(id: number, data: ProductoRequest): Observable<ProductoVendedor> {
    return this.http.put<ProductoVendedor>(`${this.api}/vendedor/productos/${id}`, data);
  }

  ajustarStock(id: number, stock: number): Observable<ProductoVendedor> {
    return this.http.patch<ProductoVendedor>(`${this.api}/vendedor/productos/${id}/stock`, {}, { params: { stock } });
  }

  cambiarActivo(id: number, activo: boolean): Observable<ProductoVendedor> {
    return this.http.patch<ProductoVendedor>(`${this.api}/vendedor/productos/${id}/activo`, {}, { params: { activo } });
  }

  getResumenProductos(): Observable<ResumenProductos> {
    return this.http.get<ResumenProductos>(`${this.api}/vendedor/productos/resumen`);
  }

  getAlertas(): Observable<Alerta[]> {
    return this.http.get<Alerta[]>(`${this.api}/vendedor/productos/alertas`);
  }

  // Ventas (ms-ordenes)
  getOrdenes(estado?: string): Observable<OrdenVendedor[]> {
    return this.http.get<OrdenVendedor[]>(`${this.api}/vendedor/ordenes`, { params: estado ? { estado } : {} });
  }

  getResumenVentas(): Observable<ResumenVendedor> {
    return this.http.get<ResumenVendedor>(`${this.api}/vendedor/ordenes/resumen`);
  }

  marcarPreparado(ordenId: number): Observable<OrdenVendedor> {
    return this.http.patch<OrdenVendedor>(`${this.api}/vendedor/ordenes/${ordenId}/preparar`, {});
  }

  // Perfil (ms-identidad)
  getPerfil(): Observable<PerfilVendedor> {
    return this.http.get<PerfilVendedor>(`${this.api}/vendedor/perfil`);
  }

  actualizarPerfil(data: Partial<PerfilVendedor>): Observable<PerfilVendedor> {
    return this.http.put<PerfilVendedor>(`${this.api}/vendedor/perfil`, data);
  }
}
