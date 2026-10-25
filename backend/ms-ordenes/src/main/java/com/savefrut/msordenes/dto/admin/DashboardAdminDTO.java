package com.savefrut.msordenes.dto.admin;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardAdminDTO(
        BigDecimal ventasHoy,
        BigDecimal ventasMes,
        long ordenesHoy,
        Map<String, Long> ordenesPorEstado,
        long clientes,
        long vendedoresAprobados,
        long vendedoresPendientes,
        List<VentaDiariaDTO> ventasSemana,
        List<ProductoVendidoDTO> topProductos
) {}
