package com.savefrut.mscatalogo.dto.vendedor;

import java.math.BigDecimal;

public record DashboardMetricasDTO(
        BigDecimal ingresosHoy,
        Long ordenesPendientes,
        Long productosStockBajo,
        Long productosActivos
) {}
