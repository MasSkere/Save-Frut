package com.savefrut.msordenes.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductoCatalogoDTO {
    private Long id;
    private String nombre;
    private Long vendedorId;
    private String nombreVendedor;
    private BigDecimal precioDescuento;
}
