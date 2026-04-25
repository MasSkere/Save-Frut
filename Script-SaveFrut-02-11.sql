USE master;
GO

-- 1. Borra la base de datos si ya existe, para empezar de cero
IF DB_ID('db_savefrut') IS NOT NULL
BEGIN
    ALTER DATABASE db_savefrut SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE db_savefrut;
END
GO

CREATE DATABASE db_savefrut;
GO

USE db_savefrut;
GO

-- ==========================================================
-- 2. CREACIÓN DE TABLAS
-- ==========================================================

CREATE TABLE roles (
    rol_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(20) NOT NULL UNIQUE
);
GO

CREATE TABLE usuarios (
    usuario_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    rol_id BIGINT NOT NULL,
    fecha_registro DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (rol_id) REFERENCES roles(rol_id)
);
GO

CREATE TABLE provincias (
    provincia_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL
);
GO

CREATE TABLE distritos (
    distrito_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    provincia_id BIGINT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    FOREIGN KEY (provincia_id) REFERENCES provincias(provincia_id)
);
GO

CREATE TABLE vendedores (
    vendedor_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    usuario_id BIGINT UNIQUE NOT NULL,
    ruc VARCHAR(11) NOT NULL UNIQUE,
    razon_social VARCHAR(255) NOT NULL,
    
    -- Campos de Ubicación (para Mapas)
    direccion VARCHAR(255) NULL, -- Dirección formateada
    distrito_id BIGINT NULL,
    latitud DECIMAL(9, 6) NULL,  -- Para el pin
    longitud DECIMAL(9, 6) NULL, -- Para el pin
    
    horario VARCHAR(100) NULL,
    logo_url VARCHAR(255) NULL,
    
    FOREIGN KEY (usuario_id) REFERENCES usuarios(usuario_id),
    FOREIGN KEY (distrito_id) REFERENCES distritos(distrito_id)
);
GO

CREATE TABLE clientes (
    cliente_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    usuario_id BIGINT UNIQUE NOT NULL,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    telefono VARCHAR(20),
    
    -- Campos de Ubicación (para Mapas y auto-llenado)
    direccion VARCHAR(255) NULL,
    distrito_id BIGINT NULL,
    latitud DECIMAL(9, 6) NULL,
    longitud DECIMAL(9, 6) NULL,

    FOREIGN KEY (usuario_id) REFERENCES usuarios(usuario_id),
    FOREIGN KEY (distrito_id) REFERENCES distritos(distrito_id)
);
GO

CREATE TABLE categorias (
    categoria_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    icono_url VARCHAR(255) NULL -- Corregido a icono_url
);
GO

CREATE TABLE tipo_medicion (
    medicion_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);
GO

CREATE TABLE productos (
    producto_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    vendedor_id BIGINT NOT NULL,
    categoria_id BIGINT NOT NULL,
    medicion_id BIGINT NOT NULL,
    
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500),
    
    precio_normal DECIMAL(10, 2) NOT NULL, -- Solo precio normal
    stock INT NOT NULL CHECK (stock >= 0),
    fecha_vencimiento DATE NOT NULL,
    
    imagen_url VARCHAR(255) NULL,
    fecha_publicacion DATETIME DEFAULT GETDATE(),
    
    FOREIGN KEY (vendedor_id) REFERENCES vendedores(vendedor_id),
    FOREIGN KEY (categoria_id) REFERENCES categorias(categoria_id),
    FOREIGN KEY (medicion_id) REFERENCES tipo_medicion(medicion_id)
);
GO

CREATE TABLE ordenes (
    orden_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    fecha_orden DATETIME DEFAULT GETDATE(),
    
    subtotal DECIMAL(10, 2) NOT NULL,
    costo_envio DECIMAL(10, 2) NOT NULL DEFAULT 0,
    tarifa_variedad DECIMAL(10, 2) NOT NULL DEFAULT 0,
    total DECIMAL(10, 2) NOT NULL,
    
    estado_orden VARCHAR(50) NOT NULL DEFAULT 'Pendiente', 
    
    direccion_envio VARCHAR(255) NOT NULL,
    distrito_envio VARCHAR(100) NOT NULL,
    
    FOREIGN KEY (cliente_id) REFERENCES clientes(cliente_id)
);
GO

CREATE TABLE detalle_orden (
    detalle_orden_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    orden_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INT NOT NULL CHECK (cantidad > 0),
    precio_unitario DECIMAL(10, 2) NOT NULL, -- El precio CON DESCUENTO
    
    FOREIGN KEY (orden_id) REFERENCES ordenes(orden_id) ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES productos(producto_id)
);
GO

-- ==========================================================
-- 3. INSERCIÓN DE DATOS
-- ==========================================================

-- Roles
INSERT INTO roles (nombre) VALUES ('ROLE_ADMIN'), ('ROLE_CLIENTE'), ('ROLE_VENDEDOR');
GO

-- Usuarios (9 usuarios con tus hashes)
INSERT INTO usuarios (email, password_hash, rol_id) VALUES 
('admin@savefrut.com', '$2a$10$M8m4TTGG1J9eUm5IDbyJvOEwCtnKdiwQtI/PkJIKim5Rn4Q7KiF.y', 1), -- ID 1
('ronal@savefrut.com', '$2a$10$AnhJ5zxKPSuP.XmmRrs3v.XZZR9sm6TCPlmkvvYsjXUwSp1gDLj6.', 2), -- ID 2
('jplopez@savefrut.com', '$2a$10$YM2lR3kxDZllZztgAbdSW.rI./5ofDZYjeYBArkjItamdPd0/FhQu', 3), -- ID 3
('metro@savefrut.com', '$2a$10$z6ka6XGT4v8PoKtmhkwgn.DtpK0Lv41Ucpr4JcHLKaV5k.5wEJka.', 3), -- ID 4
('mercado@savefrut.com', '$2a$10$oPuulvo43tcxrnV0JaOpae2cDJ2Od6uVcsGZiNZa6HYUDEqPEu68C', 3), -- ID 5
('huerta@savefrut.com', '$2a$10$VZAOC/5I6b46IUTh9AZlvOpmZIij5koPJ.106lh7snDPe5eV/vqce', 3), -- ID 6
('tottus@savefrut.com', '$2a$10$J/3eRwIWEM7wU3tWi/6fwu027SJEJ4AJhA5AwiCe.KA7Ks.b0o9MO', 3), -- ID 7
('maria@savefrut.com', '$2a$10$m/cfmqEH0gX6YTefVEztOeBIundVBKd39yqm8kSChvvdxQrrKPKQm', 3), -- ID 8
('plazavea@savefrut.com', '$2a$10$01Y.gWYzQWirJK8JT3KdxeSIUM/q/Zf0ySEPqy6xvkNA9wdooFtAK', 3); -- ID 9
GO

-- Provincias y Distritos
INSERT INTO provincias (nombre) VALUES ('Lima'), ('Callao');
GO
INSERT INTO distritos (provincia_id, nombre) VALUES
(1, 'Los Olivos'), (1, 'Miraflores'), (1, 'Surco'), (1, 'La Victoria'), 
(1, 'San Isidro'), (1, 'Cercado de Lima'), (2, 'Callao (Cercado)'), (2, 'La Perla');
GO

-- Clientes (1 Cliente con dirección)
INSERT INTO clientes (usuario_id, nombres, apellidos, telefono, direccion, distrito_id, latitud, longitud) VALUES
(2, 'Ronal', 'Martínez', '988654321', 'Av. Universitaria 5175, Los Olivos', 1, -11.96133, -77.06570);
GO

-- Vendedores (7 Vendedores, cada uno con su usuario)
INSERT INTO vendedores (usuario_id, ruc, razon_social, direccion, distrito_id, latitud, longitud, horario) VALUES
(3, '10456789012', 'Bodega Don Pepe', 'Av. Naranjal 1234, Los Olivos', 1, -11.9824, -77.0622, 'L-V: 9:00 - 18:00'),
(4, '20100053457', 'Supermercados Metro', 'Av. La Molina 1100, La Molina', 3, -12.0821, -76.9367, 'L-D: 8:00 - 22:00'),
(5, '20100065487', 'Mercado Central Puesto 12', 'Jr. Ucayali 615, Cercado de Lima', 6, -12.0464, -77.0270, 'L-S: 7:00 - 17:00'),
(6, '10987654321', 'Frutería "La Huerta"', 'Av. Benavides 456, Miraflores', 2, -12.1210, -77.0305, 'L-V: 10:00 - 19:00'),
(7, '20508876543', 'Tottus S.A.', 'Av. Angamos Este 1803, Surquillo', 3, -12.1140, -77.0218, 'L-D: 8:00 - 22:00'),
(8, '10123456789', 'Verdulería "Doña María"', 'Calle Los Pinos 215, San Isidro', 5, -12.0968, -77.0374, 'L-S: 8:00 - 18:00'),
(9, '20600789123', 'Plaza Vea Surco', 'Av. Ayacucho 1021, Santiago de Surco', 3, -12.1352, -76.9918, 'L-D: 8:00 - 22:00');
GO

-- Categorías (Corregido a icono_url)
SET IDENTITY_INSERT categorias ON;
INSERT INTO categorias (categoria_id, nombre, icono_url) VALUES
(1, 'Ácidas', 'https://placehold.co/100x100/EBF5EE/2F855A?text=Acidas'),
(2, 'Semidulces', 'https://placehold.co/100x100/EBF5EE/2F855A?text=Semidulces'),
(3, 'Dulces', 'https://placehold.co/100x100/EBF5EE/2F855A?text=Dulces'),
(4, 'Neutras', 'https://placehold.co/100x100/EBF5EE/2F855A?text=Neutras');
SET IDENTITY_INSERT categorias OFF;
GO

-- Tipo de Medición
INSERT INTO tipo_medicion (nombre) VALUES ('Kg'), ('Unidad'), ('Atado');
GO

-- Productos (Asignados a diferentes vendedores)
INSERT INTO productos 
    (vendedor_id, categoria_id, medicion_id, nombre, descripcion, precio_normal, stock, fecha_vencimiento, imagen_url)
VALUES
(1, 3, 1, 'Plátano de Seda', 'Con manchas marrones, ideal para jugos.', 4.00, 30, GETDATE(), NULL), 
(1, 1, 1, 'Maracuyá', 'Perfecto para jugo, cáscara arrugada.', 5.00, 15, GETDATE(), NULL),
(2, 3, 1, 'Mango Kent', 'Listos para consumir. Muy dulces.', 9.00, 10, GETDATE() + 1, NULL), 
(3, 4, 2, 'Palta Fuerte', 'Lista para consumir hoy o mañana.', 3.00, 25, GETDATE() + 1, NULL),
(4, 1, 2, 'Arándanos (Caja 125g)', 'Un poco blandos, para batidos.', 5.00, 10, GETDATE() + 1, NULL),
(5, 1, 1, 'Fresa', 'Ligeramente magulladas, ideal mermelada.', 6.00, 20, GETDATE() + 2, NULL), 
(6, 2, 2, 'Papaya Mediana', 'Lista para jugo, piel suave.', 7.00, 8, GETDATE() + 2, NULL),
(7, 2, 1, 'Mandarina Satsuma (1kg)', 'Fácil de pelar, muy dulce.', 5.50, 20, GETDATE() + 3, NULL),
(1, 3, 1, 'Uva Red Globe', 'Ligeramente blandas.', 8.00, 12, GETDATE() + 4, NULL), 
(2, 2, 1, 'Piña Golden', 'Piel dorada, lista para cortar.', 6.50, 5, GETDATE() + 4, NULL),
(3, 2, 1, 'Pera Packham (1kg)', 'Con algunos golpes suaves, ideal compota.', 6.00, 15, GETDATE() + 5, NULL),
(4, 1, 1, 'Naranja de Jugo (2kg)', 'Malla, cáscara fina, mucho jugo.', 7.00, 18, GETDATE() + 5, NULL),
(5, 1, 1, 'Limón Sutil', 'Cáscara un poco seca, mucho jugo.', 3.50, 50, GETDATE() + 6, NULL),
(6, 4, 1, 'Coco Rallado', 'Paquete de coco fresco.', 4.00, 10, GETDATE() + 6, NULL),
(2, 1, 1, 'Limón Tahití (1kg)','Cáscara fina, mucho jugo.', 5.00, 40, GETDATE() + 3,NULL),
(3, 3, 1, 'Uva Borgoña (1kg)','Muy dulcey y pequeñas', 9.00, 20, GETDATE() + 2,NULL),
(4, 2, 2, 'Melón Coquito','jugosos y a temporada',8.50, 10, GETDATE() + 1,NULL);
GO


-- ==========================================================
-- 4. CREACIÓN DE STORED PROCEDURES (SPs) - v12
-- ==========================================================

-- Borramos los 7 SPs por si existen
DROP PROCEDURE IF EXISTS sp_obtener_productos_disponibles;
DROP PROCEDURE IF EXISTS sp_obtener_producto_por_id;
DROP PROCEDURE IF EXISTS sp_obtener_productos_por_categoria;
DROP PROCEDURE IF EXISTS sp_obtener_productos_por_vendedor;
DROP PROCEDURE IF EXISTS sp_buscar_productos_por_nombre;
DROP PROCEDURE IF EXISTS sp_crear_pedido_v2_simple;
DROP PROCEDURE IF EXISTS sp_insertar_detalle_y_stock;
GO

-- SP 1: Para la Homepage
CREATE PROCEDURE sp_obtener_productos_disponibles @hoy DATE AS
BEGIN
    SET NOCOUNT ON;
    SELECT 
        p.producto_id AS id, p.nombre, p.descripcion, p.precio_normal AS precioNormal, 
        CAST(CASE WHEN DATEDIFF(day, @hoy, p.fecha_vencimiento) <= 2 THEN p.precio_normal * 0.40 ELSE p.precio_normal * 0.70 END AS DECIMAL(10, 2)) AS precioDescuento,
        p.fecha_vencimiento AS fechaVencimiento, 
        DATEDIFF(day, @hoy, p.fecha_vencimiento) AS diasParaVencer, 
        p.imagen_url AS imagenUrl, m.nombre AS nombreMedicion, c.nombre AS nombreCategoria,
        v.razon_social AS nombreVendedor, v.vendedor_id AS vendedorId
    FROM productos p
    JOIN vendedores v ON p.vendedor_id = v.vendedor_id
    JOIN categorias c ON p.categoria_id = c.categoria_id
    JOIN tipo_medicion m ON p.medicion_id = m.medicion_id
    WHERE p.stock > 0 AND DATEDIFF(day, @hoy, p.fecha_vencimiento) BETWEEN 0 AND 7
    ORDER BY diasParaVencer ASC;
END
GO

-- SP 2: Para el detalle de un producto
CREATE PROCEDURE sp_obtener_producto_por_id @hoy DATE, @productoId BIGINT AS
BEGIN
    SET NOCOUNT ON;
    SELECT 
        p.producto_id AS id, p.nombre, p.descripcion, p.precio_normal AS precioNormal,
        CAST(CASE WHEN DATEDIFF(day, @hoy, p.fecha_vencimiento) <= 2 THEN p.precio_normal * 0.40 ELSE p.precio_normal * 0.70 END AS DECIMAL(10, 2)) AS precioDescuento,
        p.fecha_vencimiento AS fechaVencimiento, DATEDIFF(day, @hoy, p.fecha_vencimiento) AS diasParaVencer,
        p.imagen_url AS imagenUrl, m.nombre AS nombreMedicion,
        c.nombre AS nombreCategoria, v.razon_social AS nombreVendedor, v.vendedor_id AS vendedorId
    FROM productos p
    JOIN vendedores v ON p.vendedor_id = v.vendedor_id
    JOIN categorias c ON p.categoria_id = c.categoria_id
    JOIN tipo_medicion m ON p.medicion_id = m.medicion_id
    WHERE p.producto_id = @productoId AND p.stock > 0 AND DATEDIFF(day, @hoy, p.fecha_vencimiento) BETWEEN 0 AND 7;
END
GO

-- SP 3: Para la página de categorías
CREATE PROCEDURE sp_obtener_productos_por_categoria @hoy DATE, @categoriaId BIGINT AS
BEGIN
    SET NOCOUNT ON;
    SELECT 
        p.producto_id AS id, p.nombre, p.descripcion, p.precio_normal AS precioNormal,
        CAST(CASE WHEN DATEDIFF(day, @hoy, p.fecha_vencimiento) <= 2 THEN p.precio_normal * 0.40 ELSE p.precio_normal * 0.70 END AS DECIMAL(10, 2)) AS precioDescuento,
        p.fecha_vencimiento AS fechaVencimiento, DATEDIFF(day, @hoy, p.fecha_vencimiento) AS diasParaVencer,
        p.imagen_url AS imagenUrl, m.nombre AS nombreMedicion,
        c.nombre AS nombreCategoria, v.razon_social AS nombreVendedor, v.vendedor_id AS vendedorId
    FROM productos p
    JOIN vendedores v ON p.vendedor_id = v.vendedor_id
    JOIN categorias c ON p.categoria_id = c.categoria_id
    JOIN tipo_medicion m ON p.medicion_id = m.medicion_id
    WHERE c.categoria_id = @categoriaId AND p.stock > 0 AND DATEDIFF(day, @hoy, p.fecha_vencimiento) BETWEEN 0 AND 7
    ORDER BY diasParaVencer ASC;
END
GO

-- SP 4: Para la página de tienda
CREATE PROCEDURE sp_obtener_productos_por_vendedor @hoy DATE, @vendedorId BIGINT AS
BEGIN
    SET NOCOUNT ON;
    SELECT 
        p.producto_id AS id, p.nombre, p.descripcion, p.precio_normal AS precioNormal,
        CAST(CASE WHEN DATEDIFF(day, @hoy, p.fecha_vencimiento) <= 2 THEN p.precio_normal * 0.40 ELSE p.precio_normal * 0.70 END AS DECIMAL(10, 2)) AS precioDescuento,
        p.fecha_vencimiento AS fechaVencimiento, DATEDIFF(day, @hoy, p.fecha_vencimiento) AS diasParaVencer,
        p.imagen_url AS imagenUrl, m.nombre AS nombreMedicion,
        c.nombre AS nombreCategoria, v.razon_social AS nombreVendedor, v.vendedor_id AS vendedorId
    FROM productos p
    JOIN vendedores v ON p.vendedor_id = v.vendedor_id
    JOIN categorias c ON p.categoria_id = c.categoria_id
    JOIN tipo_medicion m ON p.medicion_id = m.medicion_id
    WHERE v.vendedor_id = @vendedorId AND p.stock > 0 AND DATEDIFF(day, @hoy, p.fecha_vencimiento) BETWEEN 0 AND 7
    ORDER BY diasParaVencer ASC;
END
GO

-- SP 5: Para la búsqueda
CREATE PROCEDURE sp_buscar_productos_por_nombre @hoy DATE, @termino VARCHAR(100) AS
BEGIN
    SET NOCOUNT ON;
    IF LTRIM(RTRIM(@termino)) = '' BEGIN RETURN; END
    SET @termino = '%' + @termino + '%';
    SELECT TOP 5
        p.producto_id AS id, p.nombre, p.descripcion, p.precio_normal AS precioNormal,
        CAST(CASE WHEN DATEDIFF(day, @hoy, p.fecha_vencimiento) <= 2 THEN p.precio_normal * 0.40 ELSE p.precio_normal * 0.70 END AS DECIMAL(10, 2)) AS precioDescuento,
        p.fecha_vencimiento AS fechaVencimiento, DATEDIFF(day, @hoy, p.fecha_vencimiento) AS diasParaVencer,
        p.imagen_url AS imagenUrl, m.nombre AS nombreMedicion,
        c.nombre AS nombreCategoria, v.razon_social AS nombreVendedor, v.vendedor_id AS vendedorId
    FROM productos p
    JOIN vendedores v ON p.vendedor_id = v.vendedor_id
    JOIN categorias c ON p.categoria_id = c.categoria_id
    JOIN tipo_medicion m ON p.medicion_id = m.medicion_id
    WHERE p.nombre LIKE @termino AND p.stock > 0 AND DATEDIFF(day, @hoy, p.fecha_vencimiento) BETWEEN 0 AND 7
    ORDER BY diasParaVencer ASC;
END
GO

-- SP 6: Para el Checkout (Crear cabecera)
CREATE PROCEDURE sp_crear_pedido_v2_simple @cliente_id BIGINT, @total DECIMAL(10, 2), @direccion_envio VARCHAR(255), @nuevo_pedido_id BIGINT OUTPUT AS
BEGIN
    SET NOCOUNT ON;
    -- Este SP es simple, la API recalculará el total de todas formas.
    -- (Las columnas de tarifas en 'ordenes' las llenará la API, no el SP)
    INSERT INTO ordenes (cliente_id, fecha_orden, total, direccion_envio, estado_orden, distrito_envio)
    VALUES (@cliente_id, GETDATE(), @total, @direccion_envio, 'Pendiente', 'No especificado');
    SET @nuevo_pedido_id = SCOPE_IDENTITY();
END
GO

-- SP 7: Para el Checkout (Insertar detalle)
CREATE PROCEDURE sp_insertar_detalle_y_stock @orden_id BIGINT, @producto_id BIGINT, @cantidad INT, @precio_unitario DECIMAL(10, 2) AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @stock_actual INT;
    SELECT @stock_actual = stock FROM productos WHERE producto_id = @producto_id;
    IF (@stock_actual >= @cantidad)
    BEGIN
        BEGIN TRY
            INSERT INTO detalle_orden (orden_id, producto_id, cantidad, precio_unitario)
            VALUES (@orden_id, @producto_id, @cantidad, @precio_unitario);
            UPDATE productos SET stock = stock - @cantidad WHERE producto_id = @producto_id;
        END TRY
        BEGIN CATCH
            RAISERROR ('Error al insertar el detalle o actualizar el stock.', 16, 1);
        END CATCH
    END
    ELSE
    BEGIN
        RAISERROR ('Stock insuficiente para el producto ID: %d. Solicitado: %d, Disponible: %d', 16, 1, @producto_id, @cantidad, @stock_actual);
    END
END
GO



select * from vendedores
go