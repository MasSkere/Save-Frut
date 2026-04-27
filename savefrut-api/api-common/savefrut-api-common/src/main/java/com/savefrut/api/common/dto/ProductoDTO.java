package com.savefrut.api.common.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO (Proyección) para mostrar la información del producto en la vista del cliente.
 * Representa el resultado exacto del SP.
 */
@Data
public class ProductoDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precioNormal;
    private BigDecimal precioDescuento;
    private LocalDate fechaVencimiento;
    private Integer diasParaVencer; // (Calculado por el SP)
    private String imagenUrl;
    private String nombreMedicion;
    private String nombreCategoria;
    private String nombreVendedor;
    private Long vendedorId;

    /**
     * Constructor que Spring/JPA usará para el @SqlResultSetMapping.
     * El orden y los tipos DEBEN coincidir con el @SqlResultSetMapping
     * definido en la entidad Producto.java.
     */
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