package com.savefrut.msordenes.dto;

public record EstadisticasIdentidadDTO(
        long clientes,
        long vendedoresAprobados,
        long vendedoresPendientes
) {}
