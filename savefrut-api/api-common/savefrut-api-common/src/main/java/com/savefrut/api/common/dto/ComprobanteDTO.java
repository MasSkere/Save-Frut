package com.savefrut.api.common.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de solo datos para el comprobante de compra.
 * No contiene lógica — el mapeo lo hace {@code OrdenMapper}.
 */
@Data
public class ComprobanteDTO {

    // ─── Orden ────────────────────────────────────────────────────────────────
    private Long    ordenId;
    private LocalDateTime fechaOrden;
    private String  estadoOrden;
    private String  direccionEnvio;
    private String  distritoEnvio;

    // ─── Cliente ──────────────────────────────────────────────────────────────
    private String clienteNombres;
    private String clienteEmail;

    // ─── Totales ──────────────────────────────────────────────────────────────
    private BigDecimal subtotal;
    private BigDecimal costoEnvio;
    private BigDecimal tarifaVariedad;
    private BigDecimal total;

    // ─── Items ────────────────────────────────────────────────────────────────
    private List<DetalleItemDTO> items;
}