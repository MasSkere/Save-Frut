import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';

/** Carga una sola vez el script de Google Maps JavaScript API (agente externo). */
@Injectable({ providedIn: 'root' })
export class GoogleMapsLoaderService {
  private carga: Promise<boolean> | null = null;

  get configurado(): boolean {
    return !!environment.googleMapsApiKey;
  }

  cargar(): Promise<boolean> {
    if (!this.configurado) {
      return Promise.resolve(false);
    }
    if ((window as any).google?.maps) {
      return Promise.resolve(true);
    }
    this.carga ??= new Promise<boolean>(resolve => {
      const script = document.createElement('script');
      script.src = `https://maps.googleapis.com/maps/api/js?key=${environment.googleMapsApiKey}&language=es&region=PE`;
      script.async = true;
      script.onload = () => resolve(true);
      script.onerror = () => {
        this.carga = null;
        resolve(false);
      };
      document.head.appendChild(script);
    });
    return this.carga;
  }
}
