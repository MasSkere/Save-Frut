package com.savefrut.msordenes.dto.admin;

import java.math.BigDecimal;
import java.util.List;

public record ReporteVentasDTO(
        BigDecimal totalVentas,
        Long totalOrdenes,
        BigDecimal promedioOrden,
        List<VentaDiariaDTO> ventasDiarias
) {}

