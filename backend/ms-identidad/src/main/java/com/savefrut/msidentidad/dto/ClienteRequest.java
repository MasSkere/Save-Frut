package com.savefrut.msidentidad.dto;

import java.math.BigDecimal;

public record ClienteRequest(
        String nombres,
        String apellidos,
        String telefono,
        String direccion,
        Long distritoId,
        BigDecimal latitud,
        BigDecimal longitud
) {}
