package com.savefrut.api.vendedor.dto;

import java.math.BigDecimal;

public record DashboardMetricasDTO(
        BigDecimal ingresosHoy,
        Long ordenesPendientes,
        Long productosStockBajo,
        Long productosActivos
) {}