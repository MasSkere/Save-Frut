package com.savefrut.msordenes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrdenDTO(
        Long id,
        Long clienteId,
        String nombreCliente,
        LocalDateTime fechaOrden,
        String estadoOrden,
        BigDecimal subtotal,
        BigDecimal costoEnvio,
        BigDecimal tarifaVariedad,
        BigDecimal total,
        String direccionEnvio,
        String distritoEnvio,
        BigDecimal latitudEnvio,
        BigDecimal longitudEnvio,
        LocalDateTime fechaPago,
        List<DetalleItemDTO> items
) {}
