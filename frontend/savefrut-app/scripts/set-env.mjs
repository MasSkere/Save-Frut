// Inyecta la configuración de despliegue (variables de entorno de Vercel) en environment.ts antes del build.
// API_URL: URL del API Gateway. GOOGLE_MAPS_API_KEY: clave de Google Maps JavaScript API.
import { readFileSync, writeFileSync } from 'node:fs';

const archivo = new URL('../src/environments/environment.ts', import.meta.url);
let contenido = readFileSync(archivo, 'utf8');

const { API_URL, GOOGLE_MAPS_API_KEY } = process.env;
if (API_URL) {
  contenido = contenido.replace(/apiUrl: '[^']*'/, `apiUrl: '${API_URL.replace(/\/$/, '')}'`);
}
if (GOOGLE_MAPS_API_KEY) {
  contenido = contenido.replace(/googleMapsApiKey: '[^']*'/, `googleMapsApiKey: '${GOOGLE_MAPS_API_KEY}'`);
}
writeFileSync(archivo, contenido);
console.log(`environment.ts -> apiUrl=${API_URL || '(sin cambio)'}, googleMapsApiKey=${GOOGLE_MAPS_API_KEY ? 'definida' : '(sin cambio)'}`);
