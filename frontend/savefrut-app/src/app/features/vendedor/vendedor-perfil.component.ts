import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { VendedorPanelService } from '../../core/services/vendedor-panel.service';
import { mensajeError } from '../../core/services/auth.service';
import { PerfilVendedor } from '../../shared/models/panel.model';
import { MapaComponent, UbicacionMapa } from '../../shared/components/mapa.component';

@Component({
  selector: 'app-vendedor-perfil',
  standalone: true,
  imports: [FormsModule, MapaComponent],
  template: `
    <h1 class="text-2xl font-bold text-gray-900 mb-6">Mi tienda</h1>
    @if (perfil(); as p) {
      <form class="card-sf p-6 max-w-2xl grid md:grid-cols-2 gap-4" (ngSubmit)="guardar()">
        <div><p class="text-xs text-gray-500">RUC</p><p class="font-medium">{{ p.ruc }}</p></div>
        <div><p class="text-xs text-gray-500">Razón social</p><p class="font-medium">{{ p.razonSocial }}</p></div>
        <div class="md:col-span-2">
          <label for="direccion" class="block text-sm font-medium text-gray-700 mb-1">Dirección</label>
          <input id="direccion" name="direccion" [(ngModel)]="p.direccion" class="input-sf" />
        </div>
        <div>
          <label for="horario" class="block text-sm font-medium text-gray-700 mb-1">Horario de atención</label>
          <input id="horario" name="horario" [(ngModel)]="p.horario" class="input-sf" placeholder="L-S: 8:00 - 18:00" />
        </div>
        <div>
          <label for="logo" class="block text-sm font-medium text-gray-700 mb-1">URL del logo</label>
          <input id="logo" name="logoUrl" type="url" [(ngModel)]="p.logoUrl" class="input-sf" />
        </div>
        <div class="md:col-span-2">
          <p class="block text-sm font-medium text-gray-700 mb-1">Ubicación de la tienda</p>
          <app-mapa [editable]="true" [lat]="p.latitud" [lng]="p.longitud" (cambio)="elegirUbicacion($event)" />
        </div>
        <div>
          <label for="lat" class="block text-sm font-medium text-gray-700 mb-1">Latitud</label>
          <input id="lat" name="latitud" type="number" step="0.000001" [(ngModel)]="p.latitud" class="input-sf" />
        </div>
        <div>
          <label for="lng" class="block text-sm font-medium text-gray-700 mb-1">Longitud</label>
          <input id="lng" name="longitud" type="number" step="0.000001" [(ngModel)]="p.longitud" class="input-sf" />
        </div>
        <div class="md:col-span-2 flex items-center justify-end gap-3">
          @if (mensaje()) { <span class="text-sm text-brand-600">{{ mensaje() }}</span> }
          <button type="submit" class="btn-primary">Guardar cambios</button>
        </div>
      </form>
    }
  `
})
export class VendedorPerfilComponent implements OnInit {
  private readonly service = inject(VendedorPanelService);
  readonly perfil = signal<PerfilVendedor | null>(null);
  readonly mensaje = signal<string | null>(null);

  ngOnInit(): void {
    this.service.getPerfil().subscribe(p => this.perfil.set({ ...p }));
  }

  elegirUbicacion(u: UbicacionMapa): void {
    this.perfil.update(p => p ? { ...p, latitud: Number(u.lat.toFixed(6)), longitud: Number(u.lng.toFixed(6)) } : p);
  }

  guardar(): void {
    const p = this.perfil();
    if (!p) return;
    this.service.actualizarPerfil({
      direccion: p.direccion, horario: p.horario, logoUrl: p.logoUrl, latitud: p.latitud, longitud: p.longitud
    }).subscribe({
      next: actualizado => { this.perfil.set({ ...actualizado }); this.mensaje.set('Cambios guardados'); },
      error: err => this.mensaje.set(mensajeError(err))
    });
  }
}
