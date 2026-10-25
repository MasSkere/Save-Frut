package com.savefrut.msidentidad.dto;

import java.math.BigDecimal;

public record VendedorDTO(
        Long id,
        String razonSocial,
        String logoUrl,
        String direccion,
        String horario,
        BigDecimal latitud,
        BigDecimal longitud,
        String nombreDistrito,
        Boolean aprobado
) {}
