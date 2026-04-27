package com.savefrut.api.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReporteVentasDTO(
        LocalDate desde,
        LocalDate hasta,
        BigDecimal total,
        Long cantidadOrdenes,
        BigDecimal promedio
) {}