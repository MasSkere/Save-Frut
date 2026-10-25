package com.savefrut.msordenes.entity;

import java.util.List;

public final class EstadoOrden {

    public static final String PENDIENTE_PAGO = "PENDIENTE_PAGO";
    public static final String PAGADO = "PAGADO";
    public static final String PREPARADO = "PREPARADO";
    public static final String ENTREGADO = "ENTREGADO";
    public static final String CANCELADO = "CANCELADO";

    /** Estados que cuentan como venta concretada. */
    public static final List<String> VENTA_CONFIRMADA = List.of(PAGADO, PREPARADO, ENTREGADO);

    public static final List<String> TODOS = List.of(PENDIENTE_PAGO, PAGADO, PREPARADO, ENTREGADO, CANCELADO);

    private EstadoOrden() {
    }
}
