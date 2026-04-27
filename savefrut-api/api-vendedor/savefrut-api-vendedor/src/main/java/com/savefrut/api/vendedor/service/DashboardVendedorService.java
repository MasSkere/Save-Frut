package com.savefrut.api.vendedor.service;

import com.savefrut.api.vendedor.dto.*;
import com.savefrut.api.common.entity.*;
import com.savefrut.api.common.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardVendedorService {

    private final OrdenRepository ordenRepository;
    private final ProductoRepository productoRepository;
    private final VendedorRepository vendedorRepository;
    private final DetalleOrdenRepository detalleOrdenRepository;

    @Transactional(readOnly = true)
    public DashboardMetricasDTO getMetricas(Long vendedorId) {
        LocalDate hoy = LocalDate.now();

        BigDecimal ingresosHoy = detalleOrdenRepository
                .sumIngresosHoyByVendedor(vendedorId, hoy.atStartOfDay());

        long ordenesPendientes = detalleOrdenRepository
                .countOrdenesPendientesByVendedor(vendedorId);

        long productosStockBajo = productoRepository
                .countByVendedorVendedorIdAndStockLessThan(vendedorId, 10);

        long productosActivos = productoRepository
                .countByVendedorVendedorIdAndBajaFalse(vendedorId);

        return new DashboardMetricasDTO(
                ingresosHoy != null ? ingresosHoy : BigDecimal.ZERO,
                ordenesPendientes,
                productosStockBajo,
                productosActivos
        );
    }

    @Transactional(readOnly = true)
    public List<VentaDiariaDTO> getVentasSemanales(Long vendedorId) {
        LocalDate hoy = LocalDate.now();
        return java.util.stream.IntStream.rangeClosed(0, 6)
                .mapToObj(i -> {
                    LocalDate fecha = hoy.minusDays(i);
                    BigDecimal total = detalleOrdenRepository
                            .sumIngresosByVendedor(vendedorId, fecha.atStartOfDay());
                    long cantidad = detalleOrdenRepository
                            .countOrdenesByVendedor(vendedorId, fecha.atStartOfDay());
                    return new VentaDiariaDTO(
                            fecha,
                            total != null ? total : BigDecimal.ZERO,
                            cantidad
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoVendedorDTO> getTopProductos(Long vendedorId) {
        return productoRepository.findTop3ByVendedorId(vendedorId, PageRequest.of(0, 3)).stream()
                .map(this::toProductoDTO)
                .toList();
    }

    private ProductoVendedorDTO toProductoDTO(Producto p) {
        int dias = LocalDate.now().until(p.getFechaVencimiento()).getDays();
        BigDecimal descuento = dias <= 2
                ? p.getPrecioNormal().multiply(new BigDecimal("0.40"))
                : p.getPrecioNormal().multiply(new BigDecimal("0.70"));

        return new ProductoVendedorDTO(
                p.getId(),
                p.getNombre(),
                p.getDescripcion(),
                p.getPrecioNormal(),
                descuento.setScale(2, java.math.RoundingMode.HALF_UP),
                p.getStock(),
                p.getFechaVencimiento(),
                p.getImagenUrl(),
                p.getCategoria().getNombre(),
                !p.getBaja()
        );
    }
}