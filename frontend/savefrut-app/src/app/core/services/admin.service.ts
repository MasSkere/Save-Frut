import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Orden } from '../../shared/models/orden.model';
import {
  Alerta, DashboardAdmin, EstadisticasPuntos, ProductoAdmin, ProductoRequest, ResumenProductos, UsuarioAdmin, VendedorAdmin
} from '../../shared/models/panel.model';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}`;

  // Dashboard (ms-ordenes + ms-catalogo + ms-puntos)
  getDashboard(): Observable<DashboardAdmin> {
    return this.http.get<DashboardAdmin>(`${this.api}/admin/dashboard`);
  }

  getResumenProductos(): Observable<ResumenProductos> {
    return this.http.get<ResumenProductos>(`${this.api}/admin/productos/resumen`);
  }

  getAlertas(): Observable<Alerta[]> {
    return this.http.get<Alerta[]>(`${this.api}/admin/productos/alertas`);
  }

  getEstadisticasPuntos(): Observable<EstadisticasPuntos> {
    return this.http.get<EstadisticasPuntos>(`${this.api}/puntos/estadisticas`);
  }

  // Usuarios y vendedores (ms-identidad)
  getUsuarios(tipo?: string): Observable<UsuarioAdmin[]> {
    return this.http.get<UsuarioAdmin[]>(`${this.api}/admin/usuarios`, { params: tipo ? { tipo } : {} });
  }

  cambiarEstadoUsuario(id: number, estado: string): Observable<UsuarioAdmin> {
    return this.http.patch<UsuarioAdmin>(`${this.api}/admin/usuarios/${id}/estado`, {}, { params: { estado } });
  }

  getVendedores(): Observable<VendedorAdmin[]> {
    return this.http.get<VendedorAdmin[]>(`${this.api}/admin/vendedores`);
  }

  aprobarVendedor(id: number, aprobado: boolean): Observable<VendedorAdmin> {
    return this.http.patch<VendedorAdmin>(`${this.api}/admin/vendedores/${id}/aprobacion`, {}, { params: { aprobado } });
  }

  // Productos (ms-catalogo)
  getProductos(): Observable<ProductoAdmin[]> {
    return this.http.get<ProductoAdmin[]>(`${this.api}/admin/productos`);
  }

  crearProducto(data: ProductoRequest): Observable<ProductoAdmin> {
    return this.http.post<ProductoAdmin>(`${this.api}/admin/productos`, data);
  }

  darDeBajaProducto(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/admin/productos/${id}`);
  }

  // Órdenes (ms-ordenes)
  getOrdenes(estado?: string): Observable<Orden[]> {
    return this.http.get<Orden[]>(`${this.api}/admin/ordenes`, { params: estado ? { estado } : {} });
  }

  cambiarEstadoOrden(id: number, estado: string): Observable<Orden> {
    return this.http.patch<Orden>(`${this.api}/admin/ordenes/${id}/estado`, {}, { params: { estado } });
  }

  cancelarOrden(id: number, razon: string): Observable<Orden> {
    return this.http.post<Orden>(`${this.api}/admin/ordenes/${id}/cancelar`, {}, { params: { razon } });
  }
}
