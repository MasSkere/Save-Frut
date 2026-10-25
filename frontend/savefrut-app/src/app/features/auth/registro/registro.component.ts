import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService, mensajeError } from '../../../core/services/auth.service';
import { CatalogoService } from '../../../core/services/catalogo.service';
import { RegistroRequest } from '../../../shared/models/auth.model';
import { Ubicacion } from '../../../shared/models/producto.model';

@Component({
  selector: 'app-registro',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './registro.component.html'
})
export class RegistroComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly catalogoService = inject(CatalogoService);
  private readonly router = inject(Router);

  readonly enviando = signal(false);
  readonly error = signal<string | null>(null);
  readonly exito = signal<string | null>(null);
  readonly provincias = signal<Ubicacion[]>([]);
  readonly distritos = signal<Ubicacion[]>([]);

  form: RegistroRequest = { email: '', password: '', rol: 'CLIENTE', distritoId: null };
  provinciaId: number | null = null;

  ngOnInit(): void {
    this.catalogoService.getProvincias().subscribe(p => this.provincias.set(p));
  }

  cambiarProvincia(): void {
    this.form.distritoId = null;
    this.distritos.set([]);
    if (this.provinciaId) {
      this.catalogoService.getDistritos(this.provinciaId).subscribe(d => this.distritos.set(d));
    }
  }

  onSubmit(): void {
    this.error.set(null);
    this.enviando.set(true);
    this.authService.register(this.form).subscribe({
      next: r => {
        this.enviando.set(false);
        if (this.form.rol === 'VENDEDOR') {
          this.exito.set(r.message);
        } else {
          this.router.navigate(['/login'], { queryParams: { registrado: 1 } });
        }
      },
      error: err => {
        this.enviando.set(false);
        this.error.set(mensajeError(err, 'No se pudo completar el registro'));
      }
    });
  }
}
