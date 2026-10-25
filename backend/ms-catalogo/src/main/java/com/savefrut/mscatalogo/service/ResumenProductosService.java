package com.savefrut.mscatalogo.service;

import com.savefrut.mscatalogo.dto.AlertaDTO;
import com.savefrut.mscatalogo.dto.ResumenProductosDTO;
import com.savefrut.mscatalogo.entity.Producto;
import com.savefrut.mscatalogo.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;

/**
 * Métricas de inventario para los dashboards de vendedor y administrador.
 */
@Service
@RequiredArgsConstructor
public class ResumenProductosService {

    private static final int STOCK_BAJO = 10;
    private static final int DIAS_POR_VENCER = 2;

    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public ResumenProductosDTO getResumen(Long vendedorId) {
        List<Producto> productos = productosDe(vendedorId);
        LocalDate hoy = LocalDate.now();

        return new ResumenProductosDTO(
                contar(productos, p -> !p.getBaja()),
                contar(productos, p -> !p.getBaja() && p.getStock() < STOCK_BAJO),
                contar(productos, p -> !p.getBaja() && porVencer(p, hoy)),
                contar(productos, p -> !p.getBaja() && p.getFechaVencimiento().isBefore(hoy))
        );
    }

    @Transactional(readOnly = true)
    public List<AlertaDTO> getAlertas(Long vendedorId) {
        LocalDate hoy = LocalDate.now();
        return productosDe(vendedorId).stream()
                .filter(p -> !p.getBaja())
                .filter(p -> p.getStock() < STOCK_BAJO || porVencer(p, hoy) || p.getFechaVencimiento().isBefore(hoy))
                .map(p -> toAlerta(p, hoy))
                .toList();
    }

    private List<Producto> productosDe(Long vendedorId) {
        return vendedorId == null
                ? productoRepository.findAll()
                : productoRepository.findAllByVendedorId(vendedorId);
    }

    private boolean porVencer(Producto p, LocalDate hoy) {
        return !p.getFechaVencimiento().isBefore(hoy) && !p.getFechaVencimiento().isAfter(hoy.plusDays(DIAS_POR_VENCER));
    }

    private AlertaDTO toAlerta(Producto p, LocalDate hoy) {
        if (p.getFechaVencimiento().isBefore(hoy)) {
            return new AlertaDTO("VENCIDO", "Producto vencido: " + p.getNombre(), p.getId());
        }
        if (porVencer(p, hoy)) {
            return new AlertaDTO("POR_VENCER", "Próximo a vencer: " + p.getNombre(), p.getId());
        }
        return new AlertaDTO("STOCK_BAJO", "Stock bajo (" + p.getStock() + "): " + p.getNombre(), p.getId());
    }

    private long contar(List<Producto> productos, Predicate<Producto> filtro) {
        return productos.stream().filter(filtro).count();
    }
}
