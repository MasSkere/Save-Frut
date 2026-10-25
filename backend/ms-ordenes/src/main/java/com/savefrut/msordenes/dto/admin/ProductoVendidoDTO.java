package com.savefrut.msordenes.dto.admin;

public record ProductoVendidoDTO(
        Long id,
        String nombre,
        Long cantidadVendida,
        java.math.BigDecimal revenue
) {}
