--- No es necesario CREATE DATABASE si lo definiste en el .env,
-- el contenedor lo crea antes de ejecutar este script.

-- ==========================================================
-- 2. CREACIÓN DE TABLAS
-- ==========================================================

CREATE TABLE roles (
                       rol_id BIGSERIAL PRIMARY KEY,
                       nombre VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE usuarios (
                           usuario_id BIGSERIAL PRIMARY KEY,
                           email VARCHAR(100) NOT NULL UNIQUE,
                           password_hash VARCHAR(255) NOT NULL,
                           rol_id BIGINT NOT NULL,
                           estado VARCHAR(20) DEFAULT 'ACTIVO',
                           fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           CONSTRAINT fk_rol FOREIGN KEY (rol_id) REFERENCES roles(rol_id)
);

CREATE TABLE provincias (
                            provincia_id BIGSERIAL PRIMARY KEY,
                            nombre VARCHAR(100) NOT NULL
);

CREATE TABLE distritos (
                           distrito_id BIGSERIAL PRIMARY KEY,
                           provincia_id BIGINT NOT NULL,
                           nombre VARCHAR(100) NOT NULL,
                           CONSTRAINT fk_provincia FOREIGN KEY (provincia_id) REFERENCES provincias(provincia_id)
);

CREATE TABLE vendedores (
                            vendedor_id BIGSERIAL PRIMARY KEY,
                            usuario_id BIGINT UNIQUE NOT NULL,
                            ruc VARCHAR(11) NOT NULL UNIQUE,
                            razon_social VARCHAR(255) NOT NULL,
                            direccion VARCHAR(255) NULL,
                            distrito_id BIGINT NULL,
                            latitud DECIMAL(9, 6) NULL,
                            longitud DECIMAL(9, 6) NULL,
                            horario VARCHAR(100) NULL,
                            logo_url VARCHAR(255) NULL,
                            aprobado BOOLEAN DEFAULT FALSE,
                            CONSTRAINT fk_usuario_vendedor FOREIGN KEY (usuario_id) REFERENCES usuarios(usuario_id),
                            CONSTRAINT fk_distrito_vendedor FOREIGN KEY (distrito_id) REFERENCES distritos(distrito_id)
);

CREATE TABLE clientes (
                          cliente_id BIGSERIAL PRIMARY KEY,
                          usuario_id BIGINT UNIQUE NOT NULL,
                          nombres VARCHAR(100) NOT NULL,
                          apellidos VARCHAR(100) NOT NULL,
                          telefono VARCHAR(20),
                          direccion VARCHAR(255) NULL,
                          distrito_id BIGINT NULL,
                          latitud DECIMAL(9, 6) NULL,
                          longitud DECIMAL(9, 6) NULL,
                          CONSTRAINT fk_usuario_cliente FOREIGN KEY (usuario_id) REFERENCES usuarios(usuario_id),
                          CONSTRAINT fk_distrito_cliente FOREIGN KEY (distrito_id) REFERENCES distritos(distrito_id)
);

CREATE TABLE categorias (
                            categoria_id BIGSERIAL PRIMARY KEY,
                            nombre VARCHAR(100) NOT NULL UNIQUE,
                            icono_url VARCHAR(255) NULL
);

CREATE TABLE tipo_medicion (
                               medicion_id BIGSERIAL PRIMARY KEY,
                               nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE productos (
                           producto_id BIGSERIAL PRIMARY KEY,
                           vendedor_id BIGINT NOT NULL,
                           categoria_id BIGINT NOT NULL,
                           medicion_id BIGINT NOT NULL,
                           nombre VARCHAR(100) NOT NULL,
                           descripcion VARCHAR(500),
                           precio_normal DECIMAL(10, 2) NOT NULL,
                           stock INT NOT NULL CHECK (stock >= 0),
                           fecha_vencimiento DATE NOT NULL,
                           imagen_url VARCHAR(255) NULL,
                           fecha_publicacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           baja BOOLEAN DEFAULT FALSE,
                           CONSTRAINT fk_vendedor FOREIGN KEY (vendedor_id) REFERENCES vendedores(vendedor_id),
                           CONSTRAINT fk_categoria FOREIGN KEY (categoria_id) REFERENCES categorias(categoria_id),
                           CONSTRAINT fk_medicion FOREIGN KEY (medicion_id) REFERENCES tipo_medicion(medicion_id)
);

CREATE TABLE ordenes (
                         orden_id BIGSERIAL PRIMARY KEY,
                         cliente_id BIGINT NOT NULL,
                         fecha_orden TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         subtotal DECIMAL(10, 2) NOT NULL,
                         costo_envio DECIMAL(10, 2) NOT NULL DEFAULT 0,
                         tarifa_variedad DECIMAL(10, 2) NOT NULL DEFAULT 0,
                         total DECIMAL(10, 2) NOT NULL,
                         estado_orden VARCHAR(50) NOT NULL DEFAULT 'Pendiente',
                         direccion_envio VARCHAR(255) NOT NULL,
                         distrito_envio VARCHAR(100) NOT NULL,
                         CONSTRAINT fk_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(cliente_id)
);

CREATE TABLE detalle_orden (
                               detalle_orden_id BIGSERIAL PRIMARY KEY,
                               orden_id BIGINT NOT NULL,
                               producto_id BIGINT NOT NULL,
                               cantidad INT NOT NULL CHECK (cantidad > 0),
                               precio_unitario DECIMAL(10, 2) NOT NULL,
                               CONSTRAINT fk_orden FOREIGN KEY (orden_id) REFERENCES ordenes(orden_id) ON DELETE CASCADE,
                               CONSTRAINT fk_producto FOREIGN KEY (producto_id) REFERENCES productos(producto_id)
);

-- ==========================================================
-- 3. INSERCIÓN DE DATOS (Mantenemos tus datos)
-- ==========================================================

INSERT INTO roles (nombre) VALUES ('ROLE_ADMIN'), ('ROLE_CLIENTE'), ('ROLE_VENDEDOR');

INSERT INTO usuarios (email, password_hash, rol_id) VALUES
                                                        ('admin@savefrut.com', '$2a$10$M8m4TTGG1J9eUm5IDbyJvOEwCtnKdiwQtI/PkJIKim5Rn4Q7KiF.y', 1),
                                                        ('ronal@savefrut.com', '$2a$10$AnhJ5zxKPSuP.XmmRrs3v.XZZR9sm6TCPlmkvvYsjXUwSp1gDLj6.', 2),
                                                        ('jplopez@savefrut.com', '$2a$10$YM2lR3kxDZllZztgAbdSW.rI./5ofDZYjeYBArkjItamdPd0/FhQu', 3),
                                                        ('metro@savefrut.com', '$2a$10$z6ka6XGT4v8PoKtmhkwgn.DtpK0Lv41Ucpr4JcHLKaV5k.5wEJka.', 3),
                                                        ('mercado@savefrut.com', '$2a$10$oPuulvo43tcxrnV0JaOpae2cDJ2Od6uVcsGZiNZa6HYUDEqPEu68C', 3),
                                                        ('huerta@savefrut.com', '$2a$10$VZAOC/5I6b46IUTh9AZlvOpmZIij5koPJ.106lh7snDPe5eV/vqce', 3),
                                                        ('tottus@savefrut.com', '$2a$10$J/3eRwIWEM7wU3tWi/6fwu027SJEJ4AJhA5AwiCe.KA7Ks.b0o9MO', 3),
                                                        ('maria@savefrut.com', '$2a$10$m/cfmqEH0gX6YTefVEztOeBIundVBKd39yqm8kSChvvdxQrrKPKQm', 3),
                                                        ('plazavea@savefrut.com', '$2a$10$01Y.gWYzQWirJK8JT3KdxeSIUM/q/Zf0ySEPqy6xvkNA9wdooFtAK', 3);

INSERT INTO provincias (nombre) VALUES ('Lima'), ('Callao');

INSERT INTO distritos (provincia_id, nombre) VALUES
                                                 (1, 'Los Olivos'), (1, 'Miraflores'), (1, 'Surco'), (1, 'La Victoria'),
                                                 (1, 'San Isidro'), (1, 'Cercado de Lima'), (2, 'Callao (Cercado)'), (2, 'La Perla');

INSERT INTO categorias (categoria_id, nombre, icono_url) VALUES
                                                             (1, 'Ácidas', 'https://placehold.co/100x100/EBF5EE/2F855A?text=Acidas'),
                                                             (2, 'Semidulces', 'https://placehold.co/100x100/EBF5EE/2F855A?text=Semidulces'),
                                                             (3, 'Dulces', 'https://placehold.co/100x100/EBF5EE/2F855A?text=Dulces'),
                                                             (4, 'Neutras', 'https://placehold.co/100x100/EBF5EE/2F855A?text=Neutras');


INSERT INTO tipo_medicion (nombre) VALUES ('Kg'), ('Unidad'), ('Atado');

INSERT INTO vendedores (usuario_id, ruc, razon_social, direccion, distrito_id, latitud, longitud, horario) VALUES
                                                                                                               (3, '10456789012', 'Bodega Don Pepe', 'Av. Naranjal 1234, Los Olivos', 1, -11.9824, -77.0622, 'L-V: 9:00 - 18:00'),
                                                                                                               (4, '20100053457', 'Supermercados Metro', 'Av. La Molina 1100, La Molina', 3, -12.0821, -76.9367, 'L-D: 8:00 - 22:00'),
                                                                                                               (5, '20100065487', 'Mercado Central Puesto 12', 'Jr. Ucayali 615, Cercado de Lima', 6, -12.0464, -77.0270, 'L-S: 7:00 - 17:00'),
                                                                                                               (6, '10987654321', 'Frutería "La Huerta"', 'Av. Benavides 456, Miraflores', 2, -12.1210, -77.0305, 'L-V: 10:00 - 19:00'),
                                                                                                               (7, '20508876543', 'Tottus S.A.', 'Av. Angamos Este 1803, Surquillo', 3, -12.1140, -77.0218, 'L-D: 8:00 - 22:00'),
                                                                                                               (8, '10123456789', 'Verdulería "Doña María"', 'Calle Los Pinos 215, San Isidro', 5, -12.0968, -77.0374, 'L-S: 8:00 - 18:00'),
                                                                                                               (9, '20600789123', 'Plaza Vea Surco', 'Av. Ayacucho 1021, Santiago de Surco', 3, -12.1352, -76.9918, 'L-D: 8:00 - 22:00');


INSERT INTO clientes (usuario_id, nombres, apellidos, telefono, direccion, distrito_id, latitud, longitud) VALUES
    (2, 'Ronal', 'Martínez', '988654321', 'Av. Universitaria 5175, Los Olivos', 1, -11.96133, -77.06570);

INSERT INTO productos (vendedor_id, categoria_id, medicion_id, nombre, descripcion, precio_normal, stock, fecha_vencimiento) VALUES
                                                                                                                                 (1, 3, 1, 'Plátano de Seda', 'Con manchas marrones, ideal para jugos.', 4.00, 30, CURRENT_DATE),
                                                                                                                                 (1, 1, 1, 'Maracuyá', 'Perfecto para jugo, cáscara arrugada.', 5.00, 15, CURRENT_DATE),
                                                                                                                                 (2, 3, 1, 'Mango Kent', 'Listos para consumir. Muy dulces.', 9.00, 10, CURRENT_DATE + INTERVAL '1 day'),
                                                                                                                                 (3, 4, 2, 'Palta Fuerte', 'Lista para consumir hoy o mañana.', 3.00, 25, CURRENT_DATE + INTERVAL '1 day'),
                                                                                                                                 (4, 1, 2, 'Arándanos (Caja 125g)', 'Un poco blandos, para batidos.', 5.00, 10, CURRENT_DATE + INTERVAL '1 day'),
                                                                                                                                 (5, 1, 1, 'Fresa', 'Ligeramente magulladas, ideal mermelada.', 6.00, 20, CURRENT_DATE + INTERVAL '2 days'),
                                                                                                                                 (6, 2, 2, 'Papaya Mediana', 'Lista para jugo, piel suave.', 7.00, 8, CURRENT_DATE + INTERVAL '2 days'),
                                                                                                                                 (7, 2, 1, 'Mandarina Satsuma (1kg)', 'Fácil de pelar, muy dulce.', 5.50, 20, CURRENT_DATE + INTERVAL '3 days'),
                                                                                                                                 (1, 3, 1, 'Uva Red Globe', 'Ligeramente blandas.', 8.00, 12, CURRENT_DATE + INTERVAL '4 days'),
                                                                                                                                 (2, 2, 1, 'Piña Golden', 'Piel dorada, lista para cortar.', 6.50, 5, CURRENT_DATE + INTERVAL '4 days'),
                                                                                                                                 (3, 2, 1, 'Pera Packham (1kg)', 'Con algunos golpes suaves, ideal compota.', 6.00, 15, CURRENT_DATE + INTERVAL '5 days'),
                                                                                                                                 (4, 1, 1, 'Naranja de Jugo (2kg)', 'Malla, cáscara fina, mucho jugo.', 7.00, 18, CURRENT_DATE + INTERVAL '5 days'),
                                                                                                                                 (5, 1, 1, 'Limón Sutil', 'Cáscara un poco seca, mucho jugo.', 3.50, 50, CURRENT_DATE + INTERVAL '6 days'),
                                                                                                                                 (6, 4, 1, 'Coco Rallado', 'Paquete de coco fresco.', 4.00, 10, CURRENT_DATE + INTERVAL '6 days'),
                                                                                                                                 (2, 1, 1, 'Limón Tahití (1kg)', 'Cáscara fina, mucho jugo.', 5.00, 40, CURRENT_DATE + INTERVAL '3 days'),
                                                                                                                                 (3, 3, 1, 'Uva Borgoña (1kg)', 'Muy dulce y pequeñas', 9.00, 20, CURRENT_DATE + INTERVAL '2 days'),
                                                                                                                                 (4, 2, 2, 'Melón Coquito', 'Jugosos y a temporada', 8.50, 10, CURRENT_DATE + INTERVAL '1 day');

