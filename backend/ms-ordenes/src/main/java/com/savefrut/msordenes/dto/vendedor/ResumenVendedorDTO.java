package com.savefrut.msordenes.dto.vendedor;

import com.savefrut.msordenes.dto.admin.VentaDiariaDTO;

import java.math.BigDecimal;
import java.util.List;

public record ResumenVendedorDTO(
        BigDecimal ingresosHoy,
        BigDecimal ingresosSemana,
        long ordenesPorPreparar,
        long unidadesVendidas,
        List<VentaDiariaDTO> ventasSemana
) {}
