-- ==========================================================
-- BD: bd_ordenes
-- Microservicio: ms-ordenes
-- Tablas: ordenes, detalle_orden
-- ==========================================================

CREATE TABLE ordenes (
    orden_id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    fecha_orden TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    subtotal DECIMAL(10,2) NOT NULL,
    costo_envio DECIMAL(10,2) NOT NULL DEFAULT 0,
    tarifa_variedad DECIMAL(10,2) NOT NULL DEFAULT 0,
    total DECIMAL(10,2) NOT NULL,
    estado_orden VARCHAR(50) NOT NULL DEFAULT 'PENDIENTE_PAGO',
    direccion_envio VARCHAR(255) NOT NULL,
    distrito_envio VARCHAR(100) NOT NULL,
    latitud_envio DECIMAL(9,6),
    longitud_envio DECIMAL(9,6),
    mp_preference_id VARCHAR(100),
    mp_payment_id VARCHAR(100),
    fecha_pago TIMESTAMP
);

CREATE TABLE detalle_orden (
    detalle_orden_id BIGSERIAL PRIMARY KEY,
    orden_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    nombre_producto VARCHAR(100),
    vendedor_id BIGINT,
    cantidad INT NOT NULL CHECK (cantidad > 0),
    precio_unitario DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_orden_detalle FOREIGN KEY (orden_id) REFERENCES ordenes(orden_id) ON DELETE CASCADE
);

CREATE INDEX idx_ordenes_cliente ON ordenes(cliente_id);
CREATE INDEX idx_ordenes_estado ON ordenes(estado_orden);
CREATE INDEX idx_detalle_vendedor ON detalle_orden(vendedor_id);
