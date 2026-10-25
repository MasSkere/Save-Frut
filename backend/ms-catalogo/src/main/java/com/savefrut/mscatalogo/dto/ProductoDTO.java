package com.savefrut.mscatalogo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class ProductoDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precioNormal;
    private BigDecimal precioDescuento;
    private LocalDate fechaVencimiento;
    private Integer diasParaVencer;
    private String imagenUrl;
    private String nombreMedicion;
    private String nombreCategoria;
    private String nombreVendedor;
    private Long vendedorId;

    public ProductoDTO(Long id, String nombre, String descripcion,
                       BigDecimal precioNormal, BigDecimal precioDescuento,
                       LocalDate fechaVencimiento, Integer diasParaVencer,
                       String imagenUrl, String nombreMedicion,
                       String nombreCategoria, String nombreVendedor,
                       Long vendedorId) {

        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioNormal = precioNormal;
        this.precioDescuento = precioDescuento;
        this.fechaVencimiento = fechaVencimiento;
        this.diasParaVencer = diasParaVencer;
        this.imagenUrl = imagenUrl;
        this.nombreMedicion = nombreMedicion;
        this.nombreCategoria = nombreCategoria;
        this.nombreVendedor = nombreVendedor;
        this.vendedorId = vendedorId;
    }
}
