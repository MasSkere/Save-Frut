package com.savefrut.api.vendedor.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrdenVendedorDTO(
        Long id,
        LocalDateTime fechaOrden,
        String estadoOrden,
        BigDecimal total,
        String nombreCliente,
        String direccionEnvio,
        String distritoEnvio,
        java.util.List<ItemVendedorDTO> items
) {}