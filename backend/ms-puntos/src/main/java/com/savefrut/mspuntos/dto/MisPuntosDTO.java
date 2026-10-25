package com.savefrut.mspuntos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record MisPuntosDTO(
        int puntosAcumulados,
        BigDecimal montoTotalGastado,
        int frutasSalvadas,
        int totalCompras,
        String nivel,
        Integer puntosParaSiguienteNivel,
        List<MovimientoDTO> movimientos
) {
    public record MovimientoDTO(
            Long ordenId,
            int puntos,
            BigDecimal monto,
            int frutasSalvadas,
            LocalDateTime fecha
    ) {}
}
