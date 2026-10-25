package com.savefrut.mscatalogo.dto.vendedor;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductoVendedorRequest(
        String nombre,
        String descripcion,
        BigDecimal precioNormal,
        Integer stock,
        LocalDate fechaVencimiento,
        String imagenUrl,
        Long categoriaId,
        Long medicionId
) {}
