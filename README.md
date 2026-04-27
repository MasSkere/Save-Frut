# 🍓 SaveFrut - E-commerce de Frutas y Verduras

<p align="center">
  <img src="https://img.shields.io/badge/Java-21+-orange?style=flat&logo=java" alt="Java">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2-green?style=flat&logo=spring" alt="Spring Boot">
  <img src="https://img.shields.io/badge/PostgreSQL-16+-blue?style=flat&logo=postgresql" alt="PostgreSQL">
  <img src="https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker" alt="Docker">
  <img src="https://img.shields.io/badge/React-20232A?style=flat&logo=react" alt="React (Future)">
</p>

---

## 📋 Descripción

**SaveFrut** es una plataforma de e-commerce diseñada para reducir el desperdicio de alimentos conectando vendedores con productos próximos a vencer con compradores buscando precios accesibles.

### ✨ Características Principales

- **Descuentos dinámicos** por proximidad a vencimiento (hasta 60%)
- **Multi-vendedor** con tarifa por variedad
- **Pagos seguros** con Mercado Pago
- **Autenticación JWT** con refresh tokens
- **Dashboard administrativo** completo

---

## 🏗️ Arquitectura

```
savefrut-api/
├── api-common/           # Entidades, DTOs, Services, Repositories
│   ├── entity/           # JPA Entities
│   ├── dto/              # Data Transfer Objects (Records)
│   ├── service/          # Lógica de negocio
│   ├── mapper/           # MapStruct Mappers
│   └── repository/       # Spring Data JPA
├── api-client/            # API REST para clientes
│   └── controller/       # Endpoints públicos
├── api-admin/            # API REST para administradores
│   ├── controller/       # Endpoints admin
│   └── service/         # Dashboard, reportes
└── api-vendedor/        # API para vendedores (próximamente)
```

### 🔄 Flujo de Datos

```
Cliente → API Client → Service → Repository → PostgreSQL
                ↓
         DTO + Mapper
                ↓
         JSON Response
```

---

## 🛠️ Tecnologías

| Capa | Tecnología |
|------|------------|
| **Backend** | Java 21, Spring Boot 3.2 |
| **ORM** | Hibernate + JPA |
| **BD** | PostgreSQL 16+ |
| **Seguridad** | JWT, Spring Security |
| **Build** | Maven 3.8+ |
| **Cache** | Redis |
| **Pagos** | Mercado Pago SDK |
| **Contenedores** | Docker, Docker Compose |

---

## 📊 Modelo de Datos

```
usuarios (id, email, password_hash, rol_id, estado)
    ↓
clientes (id, usuario_id, nombres, apellidos, direccion)
    ↓
ordenes (id, cliente_id, total, estado_orden)
    ↓
detalle_orden (id, orden_id, producto_id, cantidad)

vendedores (id, usuario_id, ruc, razon_social, aprobado)
    ↓
productos (id, vendedor_id, categoria_id, nombre, precio, stock, fecha_vencimiento, baja)
    ↓
categorias (id, nombre)
```

---

## 📡 API Endpoints

### 📦 Productos (Público)
| Método | Endpoint | Descripción |
|-------|----------|-------------|
| GET | `/api/productos` | Lista disponible |
| GET | `/api/productos/{id}` | Detalle producto |
| GET | `/api/productos/categoria/{id}` | Por categorías |
| GET | `/api/productos/buscar?term=x` | Búsqueda |

### 🛒 Órdenes (Cliente)
| Método | Endpoint | Descripción |
|-------|----------|-------------|
| POST | `/api/ordenes` | Crear orden |
| GET | `/api/ordenes/comprobante/{id}` | Ver comprobante |

### 🔐 Autenticación
| Método | Endpoint | Descripción |
|-------|----------|-------------|
| POST | `/api/auth/login` | Login |
| POST | `/api/auth/refresh` | Refresh token |
| POST | `/api/auth/logout` | Logout |

### 🎛️ Admin (Protegido - Rol ADMIN)
| Método | Endpoint | Descripción |
|-------|----------|-------------|
| GET | `/admin/dashboard/metricas` | KPIs |
| GET | `/admin/dashboard/ventas-semanales` | Ventas 7 días |
| GET | `/admin/dashboard/top-productos` | Top 5 productos |
| GET | `/admin/dashboard/alertas` | Stock bajo, por vencer |
| GET | `/admin/ordenes` | Lista todas |
| PATCH | `/admin/ordenes/{id}/estado` | Cambiar estado |
| GET | `/admin/productos` | Gestionar productos |
| POST | `/admin/productos` | Crear producto |
| DELETE | `/admin/productos/{id}` | Baja lógica |
| GET | `/admin/usuarios` | Lista usuarios |
| PATCH | `/admin/usuarios/{id}/estado` | Bloquear/Activar |
| GET | `/admin/vendedores/pendientes` | Por aprobar |
| PATCH | `/admin/vendedores/{id}/aprobacion` | Aprobar/Rechazar |

---

## 🚀 Inicio Rápido

### Prerequisites
- Java 21+
- PostgreSQL 16+
- Docker (opcional)

### Configuración

```bash
# 1. Clonar repositorio
git clone https://github.com/tu-usuario/savefrut.git
cd savefrut

# 2. Configurar variables
cp .env.example .env
# Editar .env con credenciales

# 3. Iniciar PostgreSQL
docker-compose up -d postgres

# 4. Compilar
mvn clean install -DskipTests

# 5. Ejecutar API
cd savefrut-api/api-client
mvn spring-boot:run
```

### Variables de Entorno (.env)

```env
# Base de Datos
DB_URL=jdbc:postgresql://localhost:5432/db_savefrut
DB_USER=postgres
DB_PASS=tu_password

# JWT
JWT_SECRET=base64_string_minimo_64_caracteres
JWT_ACCESS_EXPIRATION=1800
JWT_REFRESH_EXPIRATION=604800

# Mercado Pago
MERCADO_PAGO_ACCESS_TOKEN=tu_token_mp

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
```

---

## 🧪 Testing

```bash
# Todos los tests
mvn test

# Coverage
mvn clean test jacoco:report
# Ver: target/site/jacoco/index.html
```

---

## 📈 Características Técnicas

### Clean Code Practices
- ✅ Inyección de dependencias con `@RequiredArgsConstructor`
- ✅ Transacciones con `@Transactional`
- ✅ Excepciones personalizadas
- ✅ DTOs con Records (Java 16+)
- ✅ MapStruct para mapeos
- ✅ Querys delegadas a BD (no streams en memoria)

### Seguridad
- ✅ Contraseñas hasheadas BCrypt
- ✅ JWT con refresh tokens
- ✅ Cookies httpOnly
- ✅ Validación de acceso por roles

### Rendimiento
- ✅ FetchType.LAZY por defecto
- ✅ JOIN FETCH en queries
- ✅ Índices en BD
- ✅ Caché Redis

---

## 📂 Estructura de Tablas

```sql
--Roles base (ADMIN, CLIENTE, VENDEDOR)
--Usuarios con estado (ACTIVO, BLOQUEADO)
--Productos con baja lógica
--Vendedores con aprobación
```

---

## 🤝 Contribución

1. Fork del repositorio
2. Crear Rama feature: `git checkout -b feature/nueva-funcionalidad`
3. Commit: `git commit -am 'Agregar feature'`
4. Push: `git push origin feature/nueva-funcionalidad`
5. Pull Request

---

## 📄 Licencia

MIT License - Copyright (c) 2024 SaveFrut Team

---

## 👨‍💻 Autor

**SaveFrut Team** - Plataforma de e-commerce sostenible

<p align="center">
  <strong>Made with ❤️ for a better world 🌱</strong>
</p>