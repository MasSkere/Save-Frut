package com.savefrut.api.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductoVencidoDTO(
        Long id,
        String nombre,
        LocalDate fechaVencimiento,
        Integer stock,
        BigDecimal perdidaEstimada
) {}