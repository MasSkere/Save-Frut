package com.savefrut.api.admin.dto;

import java.math.BigDecimal;

public record DashboardMetricasDTO(
        BigDecimal ventasDia,
        Long ordenesPendientes,
        Long productosStockBajo,
        Long productosPorVencer,
        Long clientesActivos,
        Long vendedoresActivos
) {}