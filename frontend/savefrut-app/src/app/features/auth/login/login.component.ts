import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService, mensajeError } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly loading = this.authService.loading;
  readonly error = signal<string | null>(null);
  readonly aviso = signal<string | null>(
    this.route.snapshot.queryParamMap.get('expirada') ? 'Tu sesión expiró, vuelve a ingresar.'
      : this.route.snapshot.queryParamMap.get('registrado') ? 'Cuenta creada. Ya puedes ingresar.' : null);

  email = '';
  password = '';

  onSubmit(): void {
    this.error.set(null);
    this.authService.login({ email: this.email.trim(), password: this.password }).subscribe({
      next: usuario => {
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
        this.router.navigateByUrl(returnUrl && usuario.rol === 'CLIENTE' ? returnUrl : this.authService.rutaInicio(usuario.rol));
      },
      error: err => this.error.set(mensajeError(err, 'No se pudo iniciar sesión'))
    });
  }
}
