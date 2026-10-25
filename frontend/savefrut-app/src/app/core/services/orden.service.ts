import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CrearOrdenRequest, Orden, PreferenciaPago } from '../../shared/models/orden.model';
import { MisPuntos } from '../../shared/models/panel.model';

const IDEMPOTENCY_PREFIX = 'savefrut_pago_';

export interface PerfilCliente {
  id: number;
  nombres: string;
  apellidos: string;
  telefono: string;
  direccion: string;
  nombreDistrito: string;
  email: string;
}

/** Flujo de compra del cliente: órdenes (ms-ordenes), pago Mercado Pago y Eco-Puntos (ms-puntos). */
@Injectable({ providedIn: 'root' })
export class OrdenService {
  private readonly http = inject(HttpClient);
  private readonly api = environment.apiUrl;

  crearOrden(request: CrearOrdenRequest): Observable<Orden> {
    return this.http.post<Orden>(`${this.api}/ordenes`, request);
  }

  misOrdenes(): Observable<Orden[]> {
    return this.http.get<Orden[]>(`${this.api}/ordenes/mis-ordenes`);
  }

  getOrden(id: number): Observable<Orden> {
    return this.http.get<Orden>(`${this.api}/ordenes/${id}`);
  }

  /**
   * Crea la preferencia de Checkout Pro. La Idempotency-Key se conserva por orden,
   * así un doble clic o un reintento devuelven la misma preferencia (sin cobro duplicado).
   */
  crearPreferencia(ordenId: number): Observable<PreferenciaPago> {
    const headers = new HttpHeaders().set('Idempotency-Key', this.idempotencyKey(ordenId));
    return this.http.post<PreferenciaPago>(`${this.api}/pagos/preferencia/${ordenId}`, {}, { headers });
  }

  confirmarPago(ordenId: number, paymentId: string): Observable<Orden> {
    return this.http.post<Orden>(`${this.api}/pagos/confirmar`, { ordenId, paymentId });
  }

  /** Pide al backend consultar a Mercado Pago si la orden ya tiene un pago aprobado. */
  verificarPago(ordenId: number): Observable<Orden> {
    return this.http.post<Orden>(`${this.api}/pagos/verificar/${ordenId}`, {});
  }

  miPerfil(): Observable<PerfilCliente> {
    return this.http.get<PerfilCliente>(`${this.api}/clientes/me`);
  }

  misPuntos(): Observable<MisPuntos> {
    return this.http.get<MisPuntos>(`${this.api}/puntos/mis-puntos`);
  }

  private idempotencyKey(ordenId: number): string {
    const clave = IDEMPOTENCY_PREFIX + ordenId;
    let valor = sessionStorage.getItem(clave);
    if (!valor) {
      valor = crypto.randomUUID();
      sessionStorage.setItem(clave, valor);
    }
    return valor;
  }
}
