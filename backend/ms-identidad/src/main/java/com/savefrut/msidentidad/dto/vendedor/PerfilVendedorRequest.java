package com.savefrut.msidentidad.dto.vendedor;

import java.math.BigDecimal;

public record PerfilVendedorRequest(
        String direccion,
        String horario,
        BigDecimal latitud,
        BigDecimal longitud,
        String logoUrl
) {}
