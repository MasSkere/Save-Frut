package com.savefrut.mscatalogo.dto;

public record AlertaDTO(
        String tipo,
        String mensaje,
        Long productoId
) {}
