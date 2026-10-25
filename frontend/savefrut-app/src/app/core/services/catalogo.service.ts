import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Categoria, Producto, TipoMedicion, Ubicacion, Vendedor } from '../../shared/models/producto.model';

/** Endpoints públicos: catálogo (ms-catalogo) y directorio de tiendas/ubicación (ms-identidad). */
@Injectable({ providedIn: 'root' })
export class CatalogoService {
  private readonly http = inject(HttpClient);
  private readonly api = environment.apiUrl;

  getProductos(): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.api}/productos`);
  }

  getProducto(id: number): Observable<Producto> {
    return this.http.get<Producto>(`${this.api}/productos/${id}`);
  }

  getProductosPorCategoria(categoriaId: number): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.api}/productos/categoria/${categoriaId}`);
  }

  getProductosPorVendedor(vendedorId: number): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.api}/productos/vendedor/${vendedorId}`);
  }

  buscar(termino: string): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.api}/productos/buscar`, { params: { term: termino } });
  }

  getCategorias(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(`${this.api}/categorias`);
  }

  getTiposMedicion(): Observable<TipoMedicion[]> {
    return this.http.get<TipoMedicion[]>(`${this.api}/tipo-medicion`);
  }

  getVendedores(): Observable<Vendedor[]> {
    return this.http.get<Vendedor[]>(`${this.api}/vendedores`);
  }

  getVendedor(id: number): Observable<Vendedor> {
    return this.http.get<Vendedor>(`${this.api}/vendedores/${id}`);
  }

  getProvincias(): Observable<Ubicacion[]> {
    return this.http.get<Ubicacion[]>(`${this.api}/ubicacion/provincias`);
  }

  getDistritos(provinciaId: number): Observable<Ubicacion[]> {
    return this.http.get<Ubicacion[]>(`${this.api}/ubicacion/distritos/${provinciaId}`);
  }
}
