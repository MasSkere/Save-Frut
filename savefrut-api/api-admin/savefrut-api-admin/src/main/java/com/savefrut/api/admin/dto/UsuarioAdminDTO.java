package com.savefrut.api.admin.dto;

import java.time.LocalDateTime;

public record UsuarioAdminDTO(
        Long id,
        String email,
        String rol,
        String estado,
        LocalDateTime fechaRegistro,
        Long clienteId,
        String nombres,
        String apellidos,
        Long vendedorId,
        String razonSocial
) {}