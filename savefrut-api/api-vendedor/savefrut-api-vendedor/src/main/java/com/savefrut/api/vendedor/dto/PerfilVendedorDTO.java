package com.savefrut.api.vendedor.dto;

import java.math.BigDecimal;

public record PerfilVendedorDTO(
        Long id,
        String ruc,
        String razonSocial,
        String direccion,
        String horario,
        BigDecimal latitud,
        BigDecimal longitud,
        String nombreDistrito,
        String logoUrl
) {}