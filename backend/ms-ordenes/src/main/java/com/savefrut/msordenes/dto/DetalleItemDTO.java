package com.savefrut.msordenes.dto;

import java.math.BigDecimal;

public record DetalleItemDTO(
        Long productoId,
        String nombreProducto,
        Long vendedorId,
        int cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotalItem
) {}
