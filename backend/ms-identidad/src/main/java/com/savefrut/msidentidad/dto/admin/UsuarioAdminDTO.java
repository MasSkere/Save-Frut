package com.savefrut.msidentidad.dto.admin;

import java.time.LocalDateTime;

public record UsuarioAdminDTO(
        Long id,
        String email,
        String tipo,
        String estado,
        LocalDateTime fechaRegistro
) {}
