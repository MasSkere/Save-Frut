-- ==========================================================
-- BD: bd_puntos
-- Microservicio: ms-puntos (Eco-Puntos)
-- Tablas: puntos_cliente, movimientos_puntos
-- ==========================================================

CREATE TABLE puntos_cliente (
    puntos_id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL UNIQUE,
    puntos_acumulados INT NOT NULL DEFAULT 0,
    monto_total_gastado DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    frutas_salvadas INT NOT NULL DEFAULT 0,
    total_compras INT NOT NULL DEFAULT 0,
    fecha_primera_compra TIMESTAMP,
    fecha_ultima_compra TIMESTAMP
);

-- Un movimiento por orden: la restricción UNIQUE garantiza que un mismo
-- OrdenPagadaEvent (reentregado por RabbitMQ) no sume puntos dos veces.
CREATE TABLE movimientos_puntos (
    movimiento_id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    orden_id BIGINT NOT NULL UNIQUE,
    puntos INT NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    frutas_salvadas INT NOT NULL DEFAULT 0,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_movimientos_cliente ON movimientos_puntos(cliente_id);
