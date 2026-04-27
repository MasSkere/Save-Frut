package com.savefrut.api.admin.dto;

import java.math.BigDecimal;

public record VendedorAdminDTO(
        Long id,
        String ruc,
        String razonSocial,
        String direccion,
        String horario,
        BigDecimal latitud,
        BigDecimal longitud,
        String nombreDistrito,
        Boolean aprobado,
        Long usuarioId,
        String email
) {}