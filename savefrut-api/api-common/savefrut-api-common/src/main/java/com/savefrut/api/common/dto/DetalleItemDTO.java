package com.savefrut.api.common.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO que representa un ítem dentro del comprobante de compra.
 * No contiene lógica — el mapeo lo hace {@code OrdenMapper}.
 */
@Data
public class DetalleItemDTO {

    private String     nombreProducto;
    private int        cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotalItem;
}