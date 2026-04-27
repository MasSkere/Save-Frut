package com.savefrut.api.admin.dto;

public record AlertaDTO(
        String tipo,
        String mensaje,
        Long entidadId,
        String entidadNombre
) {}