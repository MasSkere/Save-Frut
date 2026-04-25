package com.savefrut.api.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ItemDTO {
    private Long productoId;
    private int cantidad;
    private BigDecimal precioUnitario;
}