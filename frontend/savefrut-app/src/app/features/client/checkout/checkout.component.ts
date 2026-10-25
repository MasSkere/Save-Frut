import { Component, ElementRef, OnInit, computed, inject, signal, viewChild } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { switchMap } from 'rxjs';
import { COSTO_ENVIO_DOMICILIO, CarritoService, TARIFA_VARIEDAD } from '../../../core/services/carrito.service';
import { CatalogoService } from '../../../core/services/catalogo.service';
import { OrdenService } from '../../../core/services/orden.service';
import { mensajeError } from '../../../core/services/auth.service';
import { PreferenciaPago } from '../../../shared/models/orden.model';
import { Ubicacion, Vendedor } from '../../../shared/models/producto.model';
import { CurrencyPENPipe } from '../../../shared/pipes/currency-pen.pipe';
import { MapaComponent, UbicacionMapa } from '../../../shared/components/mapa.component';
import { CartLineComponent } from '../../../shared/components/cart-line.component';
import { IconComponent } from '../../../shared/components/icon.component';
import { direccionConDistrito } from '../../../shared/utils/producto.utils';

/** El checkout guarda aquí el ahorro de la orden: el backend no devuelve los precios normales y la boleta lo muestra. */
export const CLAVE_AHORRO_ORDEN = 'savefrut_ahorro_';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [FormsModule, RouterLink, CurrencyPENPipe, MapaComponent, CartLineComponent, IconComponent],
  templateUrl: './checkout.component.html',
  styles: `
    .opcion {
      display: flex;
      align-items: center;
      gap: 0.875rem;
      padding: 1rem;
      border: 1.5px solid var(--color-surface-border);
      border-radius: var(--radius-card);
      background: #fff;
      transition: border-color 150ms ease, background-color 150ms ease;
    }
    .opcion:hover {
      border-color: var(--color-brand-300);
    }
    .opcion-activa,
    .opcion-activa:hover {
      border-color: var(--color-brand-500);
      background: var(--color-brand-50);
    }
    .marca {
      width: 1.25rem;
      height: 1.25rem;
      border-radius: 9999px;
      border: 1.5px solid var(--color-gray-300);
      flex-shrink: 0;
      transition: border-width 150ms var(--ease-out), border-color 150ms ease;
    }
    .opcion-activa .marca {
      border: 6px solid var(--color-brand-500);
    }
  `
})
export class CheckoutComponent implements OnInit {
  private readonly carritoService = inject(CarritoService);
  private readonly catalogoService = inject(CatalogoService);
  private readonly ordenService = inject(OrdenService);
  private readonly router = inject(Router);
  private readonly alerta = viewChild<ElementRef<HTMLElement>>('alerta');
  private readonly formulario = viewChild<ElementRef<HTMLFormElement>>('formulario');

  readonly items = this.carritoService.items;
  readonly porTienda = this.carritoService.porTienda;
  readonly subtotal = this.carritoService.totalPrecio;
  readonly ahorro = this.carritoService.totalAhorro;
  readonly unidades = this.carritoService.totalItems;
  readonly cantidadTiendas = this.carritoService.cantidadTiendas;
  readonly costoEnvioDomicilio = COSTO_ENVIO_DOMICILIO;
  readonly direccionConDistrito = direccionConDistrito;

  readonly provincias = signal<Ubicacion[]>([]);
  readonly distritos = signal<Ubicacion[]>([]);
  readonly vendedores = signal<Vendedor[]>([]);
  readonly procesando = signal(false);
  readonly error = signal<string | null>(null);
  readonly ordenPendiente = signal<number | null>(null);
  readonly intentoEnvio = signal(false);

  readonly tipoEntrega = signal<'DOMICILIO' | 'RECOJO'>('DOMICILIO');
  readonly direccion = signal('');
  readonly distritoNombre = signal('');
  readonly ubicacion = signal<UbicacionMapa | null>(null);
  provinciaId: number | null = null;

  readonly costoEnvio = computed(() => this.tipoEntrega() === 'DOMICILIO' ? COSTO_ENVIO_DOMICILIO : 0);
  readonly tarifaVariedad = computed(() => this.cantidadTiendas() > 1 ? TARIFA_VARIEDAD : 0);
  readonly total = computed(() => this.subtotal() + this.costoEnvio() + this.tarifaVariedad());

  /** Para el recojo: dónde queda cada tienda del pedido. */
  readonly puntosRecojo = computed(() => this.porTienda().map(g => ({
    nombre: g.nombreVendedor,
    tienda: this.vendedores().find(v => v.id === g.vendedorId)
  })));

  ngOnInit(): void {
    if (this.carritoService.isEmpty()) {
      this.router.navigate(['/productos']);
      return;
    }
    this.catalogoService.getProvincias().subscribe(p => {
      this.provincias.set(p);
      // La mayoría de compradores está en Lima: se preselecciona para no pedir un paso de más.
      const lima = p.find(x => x.nombre.toLowerCase() === 'lima');
      if (lima && this.provinciaId === null) {
        this.provinciaId = lima.id;
        this.cambiarProvincia();
      }
    });
    this.catalogoService.getVendedores().subscribe({ next: v => this.vendedores.set(v), error: () => {} });
    this.ordenService.miPerfil().subscribe({
      next: perfil => {
        this.direccion.set(perfil.direccion ?? '');
        this.distritoNombre.set(perfil.nombreDistrito ?? '');
      },
      error: () => {}
    });
  }

  /** Punto elegido en Google Maps: guarda coordenadas y completa la dirección si se pudo geocodificar. */
  elegirUbicacion(u: UbicacionMapa): void {
    this.ubicacion.set(u);
    if (u.direccion) {
      this.direccion.set(u.direccion.split(',').slice(0, 2).join(',').trim());
    }
  }

  cambiarProvincia(): void {
    this.distritos.set([]);
    if (this.provinciaId) {
      this.catalogoService.getDistritos(this.provinciaId).subscribe(d => this.distritos.set(d));
    }
  }

  /** 1) Crea la orden (reserva stock vía Feign) 2) crea la preferencia de Mercado Pago 3) redirige al pago. */
  pagar(form: NgForm): void {
    if (form.invalid) {
      this.intentoEnvio.set(true);
      form.control.markAllAsTouched();
      queueMicrotask(() => this.formulario()?.nativeElement.querySelector<HTMLElement>('.ng-invalid:not(form)')?.focus());
      return;
    }
    this.error.set(null);
    this.procesando.set(true);
    const pestana = this.abrirPestanaPago();
    const ahorro = this.ahorro();
    this.ordenService.crearOrden({
      direccionEnvio: this.tipoEntrega() === 'RECOJO' ? 'Recojo en tienda' : this.direccion().trim(),
      distritoNombre: this.distritoNombre() || 'Lima',
      tipoEntrega: this.tipoEntrega(),
      latitud: this.tipoEntrega() === 'DOMICILIO' ? this.ubicacion()?.lat : undefined,
      longitud: this.tipoEntrega() === 'DOMICILIO' ? this.ubicacion()?.lng : undefined,
      items: this.items().map(i => ({ productoId: i.productoId, cantidad: i.cantidad }))
    }).pipe(
      switchMap(orden => {
        this.ordenPendiente.set(orden.id);
        sessionStorage.setItem(CLAVE_AHORRO_ORDEN + orden.id, String(ahorro));
        // La orden ya reservó el stock: si el pago falla se reintenta sobre ella, nunca se crea otra.
        this.carritoService.vaciar();
        return this.ordenService.crearPreferencia(orden.id);
      })
    ).subscribe({
      next: preferencia => this.irAPagar(preferencia, pestana),
      error: err => this.fallar(err, pestana, 'No se pudo iniciar el pago')
    });
  }

  /** Reintenta solo la preferencia de pago de la orden ya creada. */
  reintentarPago(): void {
    const ordenId = this.ordenPendiente();
    if (!ordenId) return;
    this.error.set(null);
    this.procesando.set(true);
    const pestana = this.abrirPestanaPago();
    this.ordenService.crearPreferencia(ordenId).subscribe({
      next: preferencia => this.irAPagar(preferencia, pestana),
      error: err => this.fallar(err, pestana, 'No se pudo iniciar el pago')
    });
  }

  /**
   * La pestaña de Mercado Pago se abre en el mismo clic (si se abre después de esperar al servidor,
   * el navegador la bloquea) y recibe la dirección del pago cuando la preferencia está lista.
   */
  private abrirPestanaPago(): Window | null {
    const pestana = window.open('', '_blank');
    if (pestana) {
      pestana.document.title = 'Conectando con Mercado Pago…';
      pestana.document.body.innerHTML =
        '<p style="font:16px system-ui,sans-serif;color:#1c3818;text-align:center;margin-top:30vh">Conectando con Mercado Pago…</p>';
    }
    return pestana;
  }

  private irAPagar(preferencia: PreferenciaPago, pestana: Window | null): void {
    this.ordenService.irAPagar(preferencia, pestana);
  }

  private fallar(err: unknown, pestana: Window | null, porDefecto: string): void {
    pestana?.close();
    this.procesando.set(false);
    this.error.set(mensajeError(err, porDefecto));
    queueMicrotask(() => this.alerta()?.nativeElement.focus());
  }
}
