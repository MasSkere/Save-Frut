// =====================================================================
//  SaveFrut - Pruebas funcionales de los endpoints principales
//  Uso:   node scripts/pruebas-endpoints.mjs  [http://localhost:8080]
//  Genera: docs/resultados-pruebas.md
// =====================================================================
import { writeFileSync, mkdirSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const GATEWAY = process.argv[2] ?? 'http://localhost:8080';
const resultados = [];
let fallidas = 0;

async function llamar(metodo, ruta, { token, body, headers = {} } = {}) {
  const inicio = performance.now();
  const res = await fetch(GATEWAY + ruta, {
    method: metodo,
    headers: {
      ...(body ? { 'Content-Type': 'application/json' } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...headers,
    },
    body: body ? JSON.stringify(body) : undefined,
  });
  const ms = Math.round(performance.now() - inicio);
  const texto = await res.text();
  let datos = texto;
  try { datos = JSON.parse(texto); } catch { /* texto plano */ }
  return { status: res.status, datos, ms, headers: res.headers };
}

async function prueba(nombre, metodo, ruta, esperado, opciones = {}, validar = () => true) {
  let r;
  let ok = false;
  let detalle = '';
  try {
    r = await llamar(metodo, ruta, opciones);
    ok = r.status === esperado && validar(r.datos);
    detalle = typeof r.datos === 'string' ? r.datos.slice(0, 80) : JSON.stringify(r.datos).slice(0, 80);
  } catch (e) {
    detalle = e.message;
  }
  if (!ok) fallidas++;
  resultados.push({ nombre, metodo, ruta, esperado, obtenido: r?.status ?? 'ERROR', ms: r?.ms ?? '-', ok, detalle });
  console.log(`${ok ? 'OK  ' : 'FAIL'} ${metodo.padEnd(6)} ${ruta.padEnd(42)} ${r?.status ?? 'ERR'} (${r?.ms ?? '-'} ms) ${ok ? '' : detalle}`);
  return r?.datos;
}

const login = async (email, password) => (await llamar('POST', '/auth/login', { body: { email, password } })).datos.token;

// ─── 1. Identidad ─────────────────────────────────────────────────────
const sufijo = Date.now();
const emailCliente = `cliente${sufijo}@savefrut.com`;
await prueba('Registro de cliente', 'POST', '/auth/register', 201, {
  body: { email: emailCliente, password: 'secreta123', rol: 'CLIENTE', nombres: 'Ana', apellidos: 'Prueba', telefono: '999888777', direccion: 'Av. Siempre Viva 123', distritoId: 1 },
});
await prueba('Registro con email duplicado', 'POST', '/auth/register', 409, {
  body: { email: emailCliente, password: 'secreta123', rol: 'CLIENTE', nombres: 'Ana', apellidos: 'Prueba' },
});
await prueba('Login con contraseña incorrecta', 'POST', '/auth/login', 401, { body: { email: emailCliente, password: 'mala' } });
const sesion = await prueba('Login de cliente (JWT + refresh cookie)', 'POST', '/auth/login', 200,
  { body: { email: emailCliente, password: 'secreta123' } }, d => !!d.token && d.rol === 'CLIENTE');
const tCliente = sesion?.token;
await prueba('Perfil del cliente autenticado', 'GET', '/clientes/me', 200, { token: tCliente }, d => d.email === emailCliente);
await prueba('Provincias (público)', 'GET', '/ubicacion/provincias', 200, {}, d => d.length > 0);
await prueba('Tiendas aprobadas (público)', 'GET', '/vendedores', 200, {}, d => d.length > 0);

// ─── 2. Catálogo ──────────────────────────────────────────────────────
const productos = await prueba('Catálogo con descuento dinámico (Redis)', 'GET', '/productos', 200, {},
  d => d.length > 0 && d.every(p => p.precioDescuento <= p.precioNormal));
await prueba('Categorías', 'GET', '/categorias', 200, {}, d => d.length > 0);
await prueba('Búsqueda de productos', 'GET', '/productos/buscar?term=a', 200);
await prueba('Producto inexistente', 'GET', '/productos/999999', 404);

// ─── 3. Seguridad ─────────────────────────────────────────────────────
await prueba('Órdenes sin token -> 401', 'GET', '/ordenes/mis-ordenes', 401);
const tVendedor = await login('jplopez@savefrut.com', '123456');
await prueba('Órdenes de cliente con rol VENDEDOR -> 403', 'GET', '/ordenes/mis-ordenes', 403, { token: tVendedor });
await prueba('Panel admin con rol CLIENTE -> 403', 'GET', '/admin/dashboard', 403, { token: tCliente });

// ─── 4. Órdenes (Feign a catálogo para stock) ─────────────────────────
const porVendedor = new Map();
(Array.isArray(productos) ? productos : []).forEach(p => { if (!porVendedor.has(p.vendedorId)) porVendedor.set(p.vendedorId, p); });
const elegidos = [...porVendedor.values()].slice(0, 2);
const orden = await prueba('Crear orden (2 tiendas -> tarifa variedad)', 'POST', '/ordenes', 201, {
  token: tCliente,
  body: {
    direccionEnvio: 'Av. Siempre Viva 123', distritoNombre: 'Los Olivos', tipoEntrega: 'DOMICILIO',
    items: elegidos.map(p => ({ productoId: p.id, cantidad: 1 })),
  },
}, d => d.estadoOrden === 'PENDIENTE_PAGO' && Number(d.costoEnvio) === 10 && (elegidos.length < 2 || Number(d.tarifaVariedad) === 5));
await prueba('Orden con stock insuficiente -> 409', 'POST', '/ordenes', 409, {
  token: tCliente,
  body: { direccionEnvio: 'X', distritoNombre: 'Y', tipoEntrega: 'RECOJO', items: [{ productoId: elegidos[0]?.id, cantidad: 99999 }] },
});
await prueba('Mis órdenes', 'GET', '/ordenes/mis-ordenes', 200, { token: tCliente }, d => d.length >= 1);
if (orden?.id) {
  await prueba('Detalle/comprobante de la orden', 'GET', `/ordenes/${orden.id}`, 200, { token: tCliente }, d => d.items.length > 0);
  await prueba('Pago sin Idempotency-Key -> 400', 'POST', `/pagos/preferencia/${orden.id}`, 400, { token: tCliente });
  const key = `test-${sufijo}`;
  const pref = await llamar('POST', `/pagos/preferencia/${orden.id}`, { token: tCliente, headers: { 'Idempotency-Key': key } });
  const configurado = pref.status === 200;
  await prueba(configurado ? 'Preferencia Mercado Pago creada' : 'Mercado Pago sin token -> 409 controlado',
    'POST', `/pagos/preferencia/${orden.id}`, configurado ? 200 : 409, { token: tCliente, headers: { 'Idempotency-Key': key } },
    d => !configurado || d.preferenceId === pref.datos.preferenceId);
}

// ─── 5. Vendedor ──────────────────────────────────────────────────────
await prueba('Vendedor: mis productos', 'GET', '/vendedor/productos', 200, { token: tVendedor }, d => d.length > 0);
await prueba('Vendedor: resumen de inventario', 'GET', '/vendedor/productos/resumen', 200, { token: tVendedor });
await prueba('Vendedor: resumen de ventas', 'GET', '/vendedor/ordenes/resumen', 200, { token: tVendedor });
await prueba('Vendedor: mi perfil', 'GET', '/vendedor/perfil', 200, { token: tVendedor });

// ─── 6. Administrador ─────────────────────────────────────────────────
const tAdmin = await login('admin@savefrut.com', 'admin');
await prueba('Admin: dashboard de ventas', 'GET', '/admin/dashboard', 200, { token: tAdmin }, d => 'ventasHoy' in d);
await prueba('Admin: resumen de inventario', 'GET', '/admin/productos/resumen', 200, { token: tAdmin });
await prueba('Admin: órdenes', 'GET', '/admin/ordenes', 200, { token: tAdmin });
await prueba('Admin: usuarios', 'GET', '/admin/usuarios', 200, { token: tAdmin });
await prueba('Admin: reporte de ventas', 'GET', '/admin/reportes/ventas', 200, { token: tAdmin });
await prueba('Admin: estadísticas Eco-Puntos (retención)', 'GET', '/puntos/estadisticas', 200, { token: tAdmin });
if (orden?.id) {
  await prueba('Admin: cancelar orden (libera stock)', 'POST', `/admin/ordenes/${orden.id}/cancelar?razon=prueba`, 200,
    { token: tAdmin }, d => d.estadoOrden === 'CANCELADO');
}

// ─── 7. Eco-Puntos ────────────────────────────────────────────────────
await prueba('Cliente: mis Eco-Puntos', 'GET', '/puntos/mis-puntos', 200, { token: tCliente }, d => 'nivel' in d);

// ─── Reporte ──────────────────────────────────────────────────────────
const total = resultados.length;
const fecha = new Date().toLocaleString('es-PE');
const md = [
  '# Resultados de pruebas funcionales - SaveFrut',
  '',
  `Fecha: ${fecha} · Gateway: \`${GATEWAY}\` · Resultado: **${total - fallidas}/${total} pruebas OK**`,
  '',
  'Generado con `node scripts/pruebas-endpoints.mjs`.',
  '',
  '| # | Prueba | Método | Endpoint | Esperado | Obtenido | Tiempo (ms) | Resultado |',
  '|---|--------|--------|----------|----------|----------|-------------|-----------|',
  ...resultados.map((r, i) => `| ${i + 1} | ${r.nombre} | ${r.metodo} | \`${r.ruta}\` | ${r.esperado} | ${r.obtenido} | ${r.ms} | ${r.ok ? '✅' : '❌'} |`),
  '',
].join('\n');
const salida = join(dirname(fileURLToPath(import.meta.url)), '..', 'docs', 'resultados-pruebas.md');
mkdirSync(dirname(salida), { recursive: true });
writeFileSync(salida, md);
console.log(`\n${total - fallidas}/${total} pruebas OK -> ${salida}`);
process.exit(fallidas ? 1 : 0);
