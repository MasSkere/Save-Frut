package com.savefrut.api.vendedor.dto;

import java.math.BigDecimal;

public record ItemVendedorDTO(
        Long id,
        String nombreProducto,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {}