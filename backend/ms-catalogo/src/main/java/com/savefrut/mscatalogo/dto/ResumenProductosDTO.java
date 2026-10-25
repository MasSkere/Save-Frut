package com.savefrut.mscatalogo.dto;

public record ResumenProductosDTO(
        long productosActivos,
        long productosStockBajo,
        long productosPorVencer,
        long productosVencidos
) {}
