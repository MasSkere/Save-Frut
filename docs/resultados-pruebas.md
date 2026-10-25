# Resultados de pruebas funcionales - SaveFrut

Fecha: 25/10/2026, 4:30:38 a. m. · Gateway: `http://localhost:8080` · Resultado: **32/32 pruebas OK**

Generado con `node scripts/pruebas-endpoints.mjs`.

| # | Prueba | Método | Endpoint | Esperado | Obtenido | Tiempo (ms) | Resultado |
|---|--------|--------|----------|----------|----------|-------------|-----------|
| 1 | Registro de cliente | POST | `/auth/register` | 201 | 201 | 292 | ✅ |
| 2 | Registro con email duplicado | POST | `/auth/register` | 409 | 409 | 16 | ✅ |
| 3 | Login con contraseña incorrecta | POST | `/auth/login` | 401 | 401 | 103 | ✅ |
| 4 | Login de cliente (JWT + refresh cookie) | POST | `/auth/login` | 200 | 200 | 128 | ✅ |
| 5 | Perfil del cliente autenticado | GET | `/clientes/me` | 200 | 200 | 62 | ✅ |
| 6 | Provincias (público) | GET | `/ubicacion/provincias` | 200 | 200 | 13 | ✅ |
| 7 | Tiendas aprobadas (público) | GET | `/vendedores` | 200 | 200 | 17 | ✅ |
| 8 | Catálogo con descuento dinámico (Redis) | GET | `/productos` | 200 | 200 | 8 | ✅ |
| 9 | Categorías | GET | `/categorias` | 200 | 200 | 7 | ✅ |
| 10 | Búsqueda de productos | GET | `/productos/buscar?term=a` | 200 | 200 | 52 | ✅ |
| 11 | Producto inexistente | GET | `/productos/999999` | 404 | 404 | 18 | ✅ |
| 12 | Órdenes sin token -> 401 | GET | `/ordenes/mis-ordenes` | 401 | 401 | 6 | ✅ |
| 13 | Órdenes de cliente con rol VENDEDOR -> 403 | GET | `/ordenes/mis-ordenes` | 403 | 403 | 26 | ✅ |
| 14 | Panel admin con rol CLIENTE -> 403 | GET | `/admin/dashboard` | 403 | 403 | 6 | ✅ |
| 15 | Crear orden (2 tiendas -> tarifa variedad) | POST | `/ordenes` | 201 | 201 | 86 | ✅ |
| 16 | Orden con stock insuficiente -> 409 | POST | `/ordenes` | 409 | 409 | 36 | ✅ |
| 17 | Mis órdenes | GET | `/ordenes/mis-ordenes` | 200 | 200 | 22 | ✅ |
| 18 | Detalle/comprobante de la orden | GET | `/ordenes/4` | 200 | 200 | 17 | ✅ |
| 19 | Pago sin Idempotency-Key -> 400 | POST | `/pagos/preferencia/4` | 400 | 400 | 6 | ✅ |
| 20 | Preferencia Mercado Pago creada | POST | `/pagos/preferencia/4` | 200 | 200 | 8 | ✅ |
| 21 | Vendedor: mis productos | GET | `/vendedor/productos` | 200 | 200 | 117 | ✅ |
| 22 | Vendedor: resumen de inventario | GET | `/vendedor/productos/resumen` | 200 | 200 | 23 | ✅ |
| 23 | Vendedor: resumen de ventas | GET | `/vendedor/ordenes/resumen` | 200 | 200 | 43 | ✅ |
| 24 | Vendedor: mi perfil | GET | `/vendedor/perfil` | 200 | 200 | 15 | ✅ |
| 25 | Admin: dashboard de ventas | GET | `/admin/dashboard` | 200 | 200 | 197 | ✅ |
| 26 | Admin: resumen de inventario | GET | `/admin/productos/resumen` | 200 | 200 | 11 | ✅ |
| 27 | Admin: órdenes | GET | `/admin/ordenes` | 200 | 200 | 20 | ✅ |
| 28 | Admin: usuarios | GET | `/admin/usuarios` | 200 | 200 | 17 | ✅ |
| 29 | Admin: reporte de ventas | GET | `/admin/reportes/ventas` | 200 | 200 | 64 | ✅ |
| 30 | Admin: estadísticas Eco-Puntos (retención) | GET | `/puntos/estadisticas` | 200 | 200 | 39 | ✅ |
| 31 | Admin: cancelar orden (libera stock) | POST | `/admin/ordenes/4/cancelar?razon=prueba` | 200 | 200 | 44 | ✅ |
| 32 | Cliente: mis Eco-Puntos | GET | `/puntos/mis-puntos` | 200 | 200 | 18 | ✅ |
