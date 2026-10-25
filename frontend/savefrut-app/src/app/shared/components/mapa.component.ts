import { AfterViewInit, Component, ElementRef, effect, inject, input, output, signal, viewChild } from '@angular/core';
import { GoogleMapsLoaderService } from '../../core/services/google-maps-loader.service';

export interface UbicacionMapa {
  lat: number;
  lng: number;
  direccion?: string;
}

const LIMA = { lat: -12.0464, lng: -77.0428 };

/**
 * Mapa de Google Maps. En modo editable el usuario elige un punto (clic o "usar mi ubicación")
 * y se obtiene la dirección por geocodificación inversa.
 */
@Component({
  selector: 'app-mapa',
  standalone: true,
  template: `
    @if (estado() === 'sin-clave') {
      <div class="rounded-button border border-dashed border-surface-border p-4 text-sm text-gray-500 text-center">
        Mapa no disponible: configura <code>GOOGLE_MAPS_API_KEY</code> en el archivo .env.
      </div>
    } @else if (estado() === 'error') {
      <div class="rounded-button bg-amber-50 border border-amber-200 p-3 text-sm text-amber-800 text-center">
        No se pudo cargar Google Maps. Puedes continuar escribiendo la dirección manualmente.
      </div>
    } @else {
      <div class="relative">
        <div #contenedor class="w-full rounded-button bg-gray-100 border border-surface-border" [style.height.px]="alto()"></div>
        @if (editable()) {
          <button type="button" (click)="usarMiUbicacion()"
                  class="absolute bottom-3 left-3 btn-secondary !py-1 !px-3 !text-xs shadow">📍 Usar mi ubicación</button>
        }
      </div>
      @if (editable()) {
        <p class="text-xs text-gray-500 mt-1">Haz clic en el mapa para marcar el punto exacto de entrega.</p>
      }
    }
  `
})
export class MapaComponent implements AfterViewInit {
  private readonly loader = inject(GoogleMapsLoaderService);
  private readonly contenedor = viewChild<ElementRef<HTMLDivElement>>('contenedor');

  readonly lat = input<number | null | undefined>(null);
  readonly lng = input<number | null | undefined>(null);
  readonly editable = input(false);
  readonly alto = input(260);
  readonly cambio = output<UbicacionMapa>();

  readonly estado = signal<'cargando' | 'listo' | 'sin-clave' | 'error'>(this.loader.configurado ? 'cargando' : 'sin-clave');

  private mapa: any;
  private marcador: any;

  constructor() {
    // Si las coordenadas llegan después (p. ej. desde una petición HTTP), se reposiciona el marcador.
    effect(() => {
      const lat = this.lat();
      const lng = this.lng();
      if (this.mapa && lat != null && lng != null) {
        this.colocarMarcador({ lat: Number(lat), lng: Number(lng) }, false);
      }
    });
  }

  async ngAfterViewInit(): Promise<void> {
    if (!this.loader.configurado) return;
    const ok = await this.loader.cargar();
    const elemento = this.contenedor()?.nativeElement;
    if (!ok || !elemento) {
      this.estado.set('error');
      return;
    }
    const g = (window as any).google;
    const inicial = this.lat() != null && this.lng() != null ? { lat: Number(this.lat()), lng: Number(this.lng()) } : LIMA;
    this.mapa = new g.maps.Map(elemento, {
      center: inicial,
      zoom: inicial === LIMA ? 12 : 16,
      streetViewControl: false,
      mapTypeControl: false,
      fullscreenControl: false
    });
    if (inicial !== LIMA) {
      this.colocarMarcador(inicial, false);
    }
    if (this.editable()) {
      this.mapa.addListener('click', (e: any) => this.colocarMarcador({ lat: e.latLng.lat(), lng: e.latLng.lng() }, true));
    }
    this.estado.set('listo');
  }

  usarMiUbicacion(): void {
    navigator.geolocation?.getCurrentPosition(pos =>
      this.colocarMarcador({ lat: pos.coords.latitude, lng: pos.coords.longitude }, true));
  }

  private colocarMarcador(punto: { lat: number; lng: number }, emitir: boolean): void {
    const g = (window as any).google;
    if (this.marcador) {
      this.marcador.setPosition(punto);
    } else {
      this.marcador = new g.maps.Marker({ position: punto, map: this.mapa });
    }
    this.mapa.panTo(punto);
    if (!emitir) return;

    new g.maps.Geocoder().geocode({ location: punto }, (resultados: any[], status: string) => {
      const direccion = status === 'OK' && resultados?.length ? resultados[0].formatted_address : undefined;
      this.cambio.emit({ ...punto, direccion });
    });
  }
}
