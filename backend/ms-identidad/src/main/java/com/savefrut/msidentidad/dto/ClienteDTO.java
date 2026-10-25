package com.savefrut.msidentidad.dto;

import java.math.BigDecimal;

public record ClienteDTO(
        Long id,
        String nombres,
        String apellidos,
        String telefono,
        String direccion,
        BigDecimal latitud,
        BigDecimal longitud,
        String nombreDistrito,
        String email
) {}
