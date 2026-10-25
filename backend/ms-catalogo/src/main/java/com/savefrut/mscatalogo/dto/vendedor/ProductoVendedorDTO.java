package com.savefrut.mscatalogo.dto.vendedor;

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
        Boolean activo,
        Long categoriaId,
        Long medicionId
) {}
