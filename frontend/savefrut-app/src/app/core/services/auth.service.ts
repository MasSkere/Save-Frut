import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, of, map, finalize } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse, RegistroRequest, Usuario, UserRole } from '../../shared/models/auth.model';

const TOKEN_KEY = 'savefrut_token';
const USER_KEY = 'savefrut_user';

/**
 * Sesión del usuario. El access token (JWT, 15 min) se guarda en memoria/localStorage;
 * el refresh token viaja en una cookie HttpOnly que el navegador envía a /auth/refresh.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly api = `${environment.apiUrl}/auth`;

  private readonly _user = signal<Usuario | null>(this.leerUsuario());
  private readonly _token = signal<string | null>(localStorage.getItem(TOKEN_KEY));
  private readonly _loading = signal(false);

  readonly user = this._user.asReadonly();
  readonly accessToken = this._token.asReadonly();
  readonly loading = this._loading.asReadonly();

  readonly isAuthenticated = computed(() => !!this._token());
  readonly userRole = computed(() => this._user()?.rol ?? null);
  readonly isCliente = computed(() => this.userRole() === 'CLIENTE');
  readonly isVendedor = computed(() => this.userRole() === 'VENDEDOR');
  readonly isAdmin = computed(() => this.userRole() === 'ADMIN');

  login(request: LoginRequest): Observable<Usuario> {
    this._loading.set(true);
    return this.http.post<LoginResponse>(`${this.api}/login`, request, { withCredentials: true }).pipe(
      map(r => this.guardarSesion(r)),
      finalize(() => this._loading.set(false))
    );
  }

  register(request: RegistroRequest): Observable<{ id: number; email: string; message: string }> {
    return this.http.post<{ id: number; email: string; message: string }>(`${this.api}/register`, request);
  }

  /** Rotación de refresh token: el backend invalida el anterior y emite uno nuevo en la cookie. */
  refresh(): Observable<string> {
    return this.http.post<LoginResponse>(`${this.api}/refresh`, {}, { withCredentials: true }).pipe(
      map(r => {
        this.guardarSesion(r);
        return r.token;
      })
    );
  }

  logout(): void {
    this.http.post(`${this.api}/logout`, {}, { withCredentials: true, responseType: 'text' })
      .pipe(catchError(() => of(null)))
      .subscribe();
    this.limpiarSesion();
    this.router.navigate(['/login']);
  }

  limpiarSesion(): void {
    this._user.set(null);
    this._token.set(null);
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  }

  rutaInicio(rol: UserRole | null = this.userRole()): string {
    switch (rol) {
      case 'ADMIN': return '/admin';
      case 'VENDEDOR': return '/vendedor';
      default: return '/';
    }
  }

  hasRole(roles: UserRole[]): boolean {
    const rol = this.userRole();
    return !!rol && roles.includes(rol);
  }

  private guardarSesion(r: LoginResponse): Usuario {
    const usuario: Usuario = { id: r.id, email: r.email, nombre: r.nombre, rol: r.rol };
    this._token.set(r.token);
    this._user.set(usuario);
    localStorage.setItem(TOKEN_KEY, r.token);
    localStorage.setItem(USER_KEY, JSON.stringify(usuario));
    return usuario;
  }

  private leerUsuario(): Usuario | null {
    try {
      const raw = localStorage.getItem(USER_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }
}

export function mensajeError(err: any, porDefecto = 'Ocurrió un error inesperado'): string {
  if (err?.status === 0) return 'No se pudo conectar con el servidor';
  return err?.error?.message ?? (typeof err?.error === 'string' && err.error ? err.error : porDefecto);
}
