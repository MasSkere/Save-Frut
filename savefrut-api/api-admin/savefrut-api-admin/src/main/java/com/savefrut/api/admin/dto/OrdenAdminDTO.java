package com.savefrut.api.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrdenAdminDTO(
        Long id,
        Long clienteId,
        String clienteNombre,
        LocalDateTime fechaOrden,
        String estadoOrden,
        BigDecimal subtotal,
        BigDecimal costoEnvio,
        BigDecimal tarifaVariedad,
        BigDecimal total,
        String direccionEnvio,
        String distritoEnvio
) {}