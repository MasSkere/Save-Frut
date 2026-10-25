package com.savefrut.msordenes.service;

import com.savefrut.msordenes.dto.EstadisticasIdentidadDTO;
import com.savefrut.msordenes.dto.admin.DashboardAdminDTO;
import com.savefrut.msordenes.dto.admin.ProductoVendidoDTO;
import com.savefrut.msordenes.dto.admin.ReporteVentasDTO;
import com.savefrut.msordenes.dto.admin.VentaDiariaDTO;
import com.savefrut.msordenes.entity.EstadoOrden;
import com.savefrut.msordenes.repository.DetalleOrdenRepository;
import com.savefrut.msordenes.repository.OrdenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.LongStream;

@Service
@RequiredArgsConstructor
public class DashboardAdminService {

    private final OrdenRepository ordenRepository;
    private final DetalleOrdenRepository detalleOrdenRepository;
    private final ConsultaUsuariosService consultaUsuariosService;

    @Transactional(readOnly = true)
    public DashboardAdminDTO getDashboard() {
        LocalDate hoy = LocalDate.now();
        EstadisticasIdentidadDTO usuarios = consultaUsuariosService.estadisticas();

        return new DashboardAdminDTO(
                ventasEntre(hoy, hoy),
                ventasEntre(hoy.withDayOfMonth(1), hoy),
                ordenRepository.countEntre(EstadoOrden.TODOS, hoy.atStartOfDay(), hoy.plusDays(1).atStartOfDay()),
                ordenesPorEstado(),
                usuarios.clientes(),
                usuarios.vendedoresAprobados(),
                usuarios.vendedoresPendientes(),
                ventasDiarias(hoy.minusDays(6), hoy),
                topProductos(5)
        );
    }

    @Transactional(readOnly = true)
    public ReporteVentasDTO getReporteVentas(LocalDate desde, LocalDate hasta) {
        if (hasta.isBefore(desde)) {
            throw new IllegalArgumentException("La fecha 'hasta' debe ser posterior a 'desde'");
        }
        BigDecimal total = ventasEntre(desde, hasta);
        long cantidad = ordenRepository.countEntre(EstadoOrden.VENTA_CONFIRMADA,
                desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());
        BigDecimal promedio = cantidad > 0
                ? total.divide(BigDecimal.valueOf(cantidad), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        return new ReporteVentasDTO(total, cantidad, promedio, ventasDiarias(desde, hasta));
    }

    private BigDecimal ventasEntre(LocalDate desde, LocalDate hasta) {
        return ordenRepository.sumTotalEntre(EstadoOrden.VENTA_CONFIRMADA,
                desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());
    }

    private List<VentaDiariaDTO> ventasDiarias(LocalDate desde, LocalDate hasta) {
        return LongStream.rangeClosed(0, ChronoUnit.DAYS.between(desde, hasta))
                .mapToObj(desde::plusDays)
                .map(fecha -> new VentaDiariaDTO(
                        fecha,
                        ventasEntre(fecha, fecha),
                        ordenRepository.countEntre(EstadoOrden.VENTA_CONFIRMADA,
                                fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay())))
                .toList();
    }

    private Map<String, Long> ordenesPorEstado() {
        Map<String, Long> conteo = new LinkedHashMap<>();
        EstadoOrden.TODOS.forEach(e -> conteo.put(e, 0L));
        ordenRepository.contarPorEstado().forEach(row -> conteo.put((String) row[0], (Long) row[1]));
        return conteo;
    }

    private List<ProductoVendidoDTO> topProductos(int limite) {
        return detalleOrdenRepository.findTopProductosVendidos(EstadoOrden.VENTA_CONFIRMADA, PageRequest.of(0, limite))
                .stream()
                .map(row -> new ProductoVendidoDTO(
                        (Long) row[0],
                        (String) row[1],
                        ((Number) row[2]).longValue(),
                        (BigDecimal) row[3]))
                .toList();
    }
}
