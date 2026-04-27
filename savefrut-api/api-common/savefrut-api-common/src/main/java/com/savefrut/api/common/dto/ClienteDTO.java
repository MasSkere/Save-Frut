package com.savefrut.api.common.dto;

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