package com.savefrut.api.admin.dto;

import java.math.BigDecimal;

public record ProductoVendidoDTO(
        Long productoId,
        String nombre,
        Long cantidadVendida,
        BigDecimal totalVendido
) {}