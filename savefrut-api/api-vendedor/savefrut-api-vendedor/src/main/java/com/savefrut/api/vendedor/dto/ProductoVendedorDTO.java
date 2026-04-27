package com.savefrut.api.vendedor.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductoVendedorDTO(
        Long id,
        String nombre,
        String descripcion,
        BigDecimal precioNormal,
        BigDecimal precioDescuento,
        Integer stock,
        LocalDate fechaVencimiento,
        String imagenUrl,
        String nombreCategoria,
        Boolean activo
) {}