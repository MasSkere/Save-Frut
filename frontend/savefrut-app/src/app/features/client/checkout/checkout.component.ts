import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { switchMap } from 'rxjs';
import { CarritoService } from '../../../core/services/carrito.service';
import { CatalogoService } from '../../../core/services/catalogo.service';
import { OrdenService } from '../../../core/services/orden.service';
import { mensajeError } from '../../../core/services/auth.service';
import { Ubicacion } from '../../../shared/models/producto.model';
import { CurrencyPENPipe } from '../../../shared/pipes/currency-pen.pipe';
import { MapaComponent, UbicacionMapa } from '../../../shared/components/mapa.component';

const COSTO_ENVIO_DOMICILIO = 10;
const TARIFA_VARIEDAD = 5;

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [FormsModule, RouterLink, CurrencyPENPipe, MapaComponent],
  templateUrl: './checkout.component.html'
})
export class CheckoutComponent implements OnInit {
  private readonly carritoService = inject(CarritoService);
  private readonly catalogoService = inject(CatalogoService);
  private readonly ordenService = inject(OrdenService);
  private readonly router = inject(Router);

  readonly items = this.carritoService.items;
  readonly subtotal = this.carritoService.totalPrecio;
  readonly ahorro = this.carritoService.totalAhorro;
  readonly cantidadTiendas = this.carritoService.cantidadTiendas;

  readonly provincias = signal<Ubicacion[]>([]);
  readonly distritos = signal<Ubicacion[]>([]);
  readonly procesando = signal(false);
  readonly error = signal<string | null>(null);
  readonly ordenPendiente = signal<number | null>(null);

  readonly tipoEntrega = signal<'DOMICILIO' | 'RECOJO'>('DOMICILIO');
  readonly direccion = signal('');
  readonly distritoNombre = signal('');
  readonly ubicacion = signal<UbicacionMapa | null>(null);
  provinciaId: number | null = null;

  readonly costoEnvio = computed(() => this.tipoEntrega() === 'DOMICILIO' ? COSTO_ENVIO_DOMICILIO : 0);
  readonly tarifaVariedad = computed(() => this.cantidadTiendas() > 1 ? TARIFA_VARIEDAD : 0);
  readonly total = computed(() => this.subtotal() + this.costoEnvio() + this.tarifaVariedad());

  ngOnInit(): void {
    if (this.carritoService.isEmpty()) {
      this.router.navigate(['/productos']);
      return;
    }
    this.catalogoService.getProvincias().subscribe(p => this.provincias.set(p));
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
  pagar(): void {
    this.error.set(null);
    this.procesando.set(true);
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
        this.carritoService.vaciar();
        return this.ordenService.crearPreferencia(orden.id);
      })
    ).subscribe({
      next: preferencia => {
        window.location.href = preferencia.initPoint || preferencia.sandboxInitPoint;
      },
      error: err => {
        this.procesando.set(false);
        this.error.set(mensajeError(err, 'No se pudo procesar el pago'));
      }
    });
  }
}
