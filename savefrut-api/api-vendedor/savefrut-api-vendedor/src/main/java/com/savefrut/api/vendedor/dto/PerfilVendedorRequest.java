package com.savefrut.api.vendedor.dto;

import java.math.BigDecimal;

public record PerfilVendedorRequest(
        String direccion,
        String horario,
        BigDecimal latitud,
        BigDecimal longitud,
        String logoUrl
) {}