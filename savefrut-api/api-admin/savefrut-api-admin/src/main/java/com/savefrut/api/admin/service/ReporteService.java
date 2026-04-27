package com.savefrut.api.admin.service;

import com.savefrut.api.admin.dto.*;
import com.savefrut.api.common.entity.*;
import com.savefrut.api.common.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReporteService {

    private final OrdenRepository ordenRepository;
    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public ReporteVentasDTO getReporteVentas(LocalDate desde, LocalDate hasta) {
        List<Orden> ordenes = ordenRepository.findAll().stream()
                .filter(o -> o.getFechaOrden().toLocalDate().isAfter(desde.minusDays(1)))
                .filter(o -> o.getFechaOrden().toLocalDate().isBefore(hasta.plusDays(1)))
                .toList();

        BigDecimal total = ordenes.stream()
                .map(Orden::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long cantidad = ordenes.size();
        BigDecimal promedio = cantidad > 0
                ? total.divide(BigDecimal.valueOf(cantidad), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new ReporteVentasDTO(desde, hasta, total, cantidad, promedio);
    }

    @Transactional(readOnly = true)
    public List<ProductoVencidoDTO> getProductosVencidos() {
        LocalDate hoy = LocalDate.now();

        return productoRepository.findAll().stream()
                .filter(p -> p.getFechaVencimiento().isBefore(hoy) && !p.getBaja())
                .map(p -> new ProductoVencidoDTO(
                        p.getId(),
                        p.getNombre(),
                        p.getFechaVencimiento(),
                        p.getStock(),
                        p.getPrecioNormal().multiply(BigDecimal.valueOf(p.getStock()))
                ))
                .toList();
    }
}