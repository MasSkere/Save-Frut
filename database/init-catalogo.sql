-- ==========================================================
-- BD: bd_catalogo
-- Microservicio: ms-catalogo
-- Tablas: categorias, tipo_medicion, productos
-- ==========================================================

CREATE TABLE categorias (
    categoria_id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    icono_url VARCHAR(255)
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
    precio_normal DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL CHECK (stock >= 0),
    version BIGINT DEFAULT 0,
    fecha_vencimiento DATE NOT NULL,
    imagen_url VARCHAR(255),
    fecha_publicacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    baja BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_categoria FOREIGN KEY (categoria_id) REFERENCES categorias(categoria_id),
    CONSTRAINT fk_medicion FOREIGN KEY (medicion_id) REFERENCES tipo_medicion(medicion_id)
);

-- SEED
INSERT INTO categorias (nombre, icono_url) VALUES
('Ácidas', 'https://placehold.co/100x100/EBF5EE/2F855A?text=Acidas'),
('Semidulces', 'https://placehold.co/100x100/EBF5EE/2F855A?text=Semidulces'),
('Dulces', 'https://placehold.co/100x100/EBF5EE/2F855A?text=Dulces'),
('Neutras', 'https://placehold.co/100x100/EBF5EE/2F855A?text=Neutras');

INSERT INTO tipo_medicion (nombre) VALUES ('Kg'), ('Unidad'), ('Atado');

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
