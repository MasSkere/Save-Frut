export interface LoginRequest {
  email: string;
  password: string;
}

/** Respuesta de POST /auth/login y /auth/refresh (ms-identidad). */
export interface LoginResponse {
  token: string;
  type: string;
  id: number;
  email: string;
  roles: string[];
  rol: UserRole;
  nombre: string;
}

export interface Usuario {
  id: number;
  email: string;
  nombre: string;
  rol: UserRole;
}

export type UserRole = 'CLIENTE' | 'VENDEDOR' | 'ADMIN';

export interface RegistroRequest {
  email: string;
  password: string;
  rol: 'CLIENTE' | 'VENDEDOR';
  nombres?: string;
  apellidos?: string;
  telefono?: string;
  ruc?: string;
  razonSocial?: string;
  direccion?: string;
  distritoId?: number | null;
}
