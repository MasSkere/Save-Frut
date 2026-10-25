export const environment = {
  production: false,
  /** Único punto de entrada al backend: API Gateway (Spring Cloud Gateway). */
  apiUrl: 'http://localhost:8080',
  /** Opcional: clave de Google Maps para el selector de ubicación en el checkout. */
  googleMapsApiKey: ''
};
