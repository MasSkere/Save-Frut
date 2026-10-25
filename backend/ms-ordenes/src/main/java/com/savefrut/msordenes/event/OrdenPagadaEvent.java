package com.savefrut.msordenes.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdenPagadaEvent implements Serializable {
    private Long ordenId;
    private Long clienteId;
    private BigDecimal total;
    private LocalDateTime fechaPago;
    /** Unidades compradas: representan las "frutas salvadas" para Eco-Puntos. */
    private Integer cantidadProductos;
}
