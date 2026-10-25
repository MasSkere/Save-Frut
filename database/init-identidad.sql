-- ==========================================================
-- BD: bd_identidad
-- Microservicio: ms-identidad
-- Tablas: roles, usuarios, provincias, distritos, vendedores, clientes
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
    direccion VARCHAR(255),
    distrito_id BIGINT,
    latitud DECIMAL(9,6),
    longitud DECIMAL(9,6),
    horario VARCHAR(100),
    logo_url VARCHAR(255),
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
    direccion VARCHAR(255),
    distrito_id BIGINT,
    latitud DECIMAL(9,6),
    longitud DECIMAL(9,6),
    CONSTRAINT fk_usuario_cliente FOREIGN KEY (usuario_id) REFERENCES usuarios(usuario_id),
    CONSTRAINT fk_distrito_cliente FOREIGN KEY (distrito_id) REFERENCES distritos(distrito_id)
);

-- SEED
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

-- Las tiendas de demostración ya están aprobadas por el administrador
UPDATE vendedores SET aprobado = TRUE;
