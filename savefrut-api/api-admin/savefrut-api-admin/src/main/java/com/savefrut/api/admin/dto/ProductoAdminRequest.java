package com.savefrut.api.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductoAdminRequest(
        String nombre,
        String descripcion,
        BigDecimal precioNormal,
        Integer stock,
        LocalDate fechaVencimiento,
        String imagenUrl,
        Long categoriaId,
        Long vendedorId,
        Long medicionId
) {}