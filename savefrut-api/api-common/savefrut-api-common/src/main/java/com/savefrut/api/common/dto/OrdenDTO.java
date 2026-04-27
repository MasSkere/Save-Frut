package com.savefrut.api.common.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrdenDTO(
        Long id,
        LocalDateTime fechaOrden,
        String estadoOrden,
        BigDecimal total,
        String direccionEnvio,
        String distritoEnvio
) {}