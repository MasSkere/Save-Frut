package com.savefrut.mscatalogo.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductoAdminDTO(
        Long id,
        String nombre,
        String descripcion,
        BigDecimal precioNormal,
        Integer stock,
        LocalDate fechaVencimiento,
        String imagenUrl,
        String nombreCategoria,
        String nombreVendedor,
        Boolean baja
) {}
