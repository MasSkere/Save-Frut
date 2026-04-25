# SaveFrut V2 - E-commerce de Frutas y Verduras Próximas a Vencer

Plataforma moderna para reducir desperdicio de alimentos conectando vendedores con compradores.

## Arquitectura

Este proyecto utiliza una arquitectura modular con Maven:

- **api-common**: Entidades, DTOs, repositorios, servicios y configuración compartida
- **api-client**: API REST para compradores (productos, pedidos, pagos)
- **api-admin**: API para administración (futuro)

## Tecnologías

- **Backend**: Spring Boot 3.5.7, Java 17, SQL Server, JWT, Mercado Pago
- **Frontend**: HTML/CSS/JavaScript vanilla (migrará a Angular)
- **Contenedorización**: Docker, Docker Compose
- **Base de Datos**: SQL Server con procedimientos almacenados

## Inicio Rápido

### Prerrequisitos

- Docker Desktop
- Java 17
- Maven 3.8+

### Configuración

1. Clona el repositorio:
```bash
git clone https://github.com/tuusuario/savefrut-v2.git
cd savefrut-v2
```

2. Configura variables de entorno:
```bash
cp .env.example .env
# Edita .env con tus valores reales
```

3. Ejecuta con Docker:
```bash
docker-compose up --build
```

La API estará disponible en `http://localhost:8091`

### Desarrollo Local

1. Configura SQL Server local o usa Docker
2. Ejecuta la API:
```bash
cd savefrut-api
mvn clean install
cd api-client
mvn spring-boot:run
```

## Estructura del Proyecto

```
savefrut-v2/
├── savefrut-api/           # Backend modular
│   ├── pom.xml            # POM padre
│   ├── api-common/        # Código compartido
│   ├── api-client/        # API para clientes
│   └── api-admin/         # API para admins
├── savefrut-client/       # Frontend (HTML/JS)
├── docker-compose.yml     # Orquestación
├── .env.example          # Variables ejemplo
└── .gitignore           # Archivos ignorados
```

## Características Principales

- ✅ Descuentos dinámicos por vencimiento
- ✅ Carrito multi-vendedor con tarifa de variedad
- ✅ Envío a domicilio + recojo en tienda
- ✅ Pago con Mercado Pago
- ✅ Autenticación JWT con refresh tokens (30 min access, 7 días refresh)
- ✅ Refresh tokens seguros en httpOnly cookies
- ✅ API modular y escalable

## Roadmap

- [x] Variables de entorno completas
- [x] JWT mejorado (30 min access, 7 días refresh)
- [ ] Mercado Pago integración
- [ ] Envíos dinámicos por zona
- [ ] Descuentos por cantidad
- [ ] Reserva de productos
- [ ] Puntos de fidelización
- [ ] Angular frontend
- [ ] API vendedor
- [ ] Sistema de comisiones

## Contribución

1. Crea una rama feature: `git checkout -b feature/nueva-funcionalidad`
2. Commit tus cambios: `git commit -m 'feat: descripción'`
3. Push a la rama: `git push origin feature/nueva-funcionalidad`
4. Crea un Pull Request

## Licencia

Este proyecto está bajo la Licencia MIT.
