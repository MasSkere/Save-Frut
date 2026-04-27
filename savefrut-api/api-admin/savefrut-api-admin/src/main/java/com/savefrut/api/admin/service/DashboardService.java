package com.savefrut.api.admin.service;

import com.savefrut.api.admin.dto.*;
import com.savefrut.api.common.entity.*;
import com.savefrut.api.common.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrdenRepository ordenRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final VendedorRepository vendedorRepository;
    private final DetalleOrdenRepository detalleOrdenRepository;

    @Transactional(readOnly = true)
    public DashboardMetricasDTO getMetricas() {
        LocalDate hoy = LocalDate.now();
        LocalDate limiteVencer = hoy.plusDays(3);

        BigDecimal ventasDia = ordenRepository.sumTotalByFecha(hoy);
        long ordenesPendientes = ordenRepository.countByEstado("Pendiente");
        long productosStockBajo = productoRepository.countByStockLessThan(10);
        long productosPorVencer = productoRepository.countPorVencer(limiteVencer);
        long clientesActivos = clienteRepository.count();
        long vendedoresActivos = vendedorRepository.countByAprobadoTrue();

        return new DashboardMetricasDTO(
                ventasDia != null ? ventasDia : BigDecimal.ZERO,
                ordenesPendientes,
                productosStockBajo,
                productosPorVencer,
                clientesActivos,
                vendedoresActivos
        );
    }

    @Transactional(readOnly = true)
    public List<VentaDiariaDTO> getVentasSemanales() {
        LocalDate hoy = LocalDate.now();
        return java.util.stream.IntStream.rangeClosed(0, 6)
                .mapToObj(i -> {
                    LocalDate fecha = hoy.minusDays(i);
                    BigDecimal total = ordenRepository.sumTotalByFecha(fecha);
                    long cantidad = ordenRepository.countByFechaAndEstado(fecha, null);
                    return new VentaDiariaDTO(
                            fecha,
                            total != null ? total : BigDecimal.ZERO,
                            cantidad
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoVendidoDTO> getTopProductos() {
        return detalleOrdenRepository.findTop5ProductosVendidos().stream()
                .map(row -> new ProductoVendidoDTO(
                        (Long) row[0],
                        (String) row[1],
                        (Long) row[2],
                        (BigDecimal) row[3]
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AlertaDTO> getAlertas() {
        LocalDate hoy = LocalDate.now();
        List<AlertaDTO> alertas = productoRepository.findAlertas(hoy.plusDays(3), 10).stream()
                .map(p -> new AlertaDTO(
                        p.getStock() < 10 ? "STOCK_BAJO" : "POR_VENCER",
                        p.getStock() < 10
                                ? "Stock bajo: " + p.getNombre()
                                : "Próximo a vencer: " + p.getNombre(),
                        p.getId(),
                        p.getNombre()
                ))
                .toList();

        return alertas;
    }
}