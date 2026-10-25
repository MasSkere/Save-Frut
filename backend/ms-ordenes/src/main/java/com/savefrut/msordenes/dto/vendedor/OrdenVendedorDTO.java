package com.savefrut.msordenes.dto.vendedor;

import com.savefrut.msordenes.dto.DetalleItemDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Orden vista por un vendedor: solo incluye sus propios ítems. */
public record OrdenVendedorDTO(
        Long id,
        LocalDateTime fechaOrden,
        String estadoOrden,
        BigDecimal totalVendedor,
        String nombreCliente,
        String direccionEnvio,
        String distritoEnvio,
        BigDecimal latitudEnvio,
        BigDecimal longitudEnvio,
        List<DetalleItemDTO> items
) {}
