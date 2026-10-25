package com.savefrut.mspuntos.dto;

import java.math.BigDecimal;
import java.util.List;

public record EstadisticasPuntosDTO(
        long clientesConCompras,
        long clientesRecurrentes,
        BigDecimal tasaRetencion,
        long frutasSalvadasTotal,
        List<RankingDTO> ranking
) {
    public record RankingDTO(
            Long clienteId,
            int puntos,
            int frutasSalvadas,
            int compras
    ) {}
}
