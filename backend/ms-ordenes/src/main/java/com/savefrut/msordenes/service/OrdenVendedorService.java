package com.savefrut.msordenes.service;

import com.savefrut.msordenes.dto.admin.VentaDiariaDTO;
import com.savefrut.msordenes.dto.vendedor.OrdenVendedorDTO;
import com.savefrut.msordenes.dto.vendedor.ResumenVendedorDTO;
import com.savefrut.msordenes.entity.DetalleOrden;
import com.savefrut.msordenes.entity.EstadoOrden;
import com.savefrut.msordenes.entity.Orden;
import com.savefrut.msordenes.repository.OrdenRepository;
import com.savefrut.msordenes.repository.DetalleOrdenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrdenVendedorService {

    private final DetalleOrdenRepository detalleOrdenRepository;
    private final OrdenRepository ordenRepository;
    private final ConsultaUsuariosService consultaUsuariosService;

    /** Órdenes pagadas que contienen productos del vendedor (solo se muestran sus ítems). */
    @Transactional(readOnly = true)
    public List<OrdenVendedorDTO> getOrdenes(Long usuarioId, String estado) {
        Long vendedorId = consultaUsuariosService.vendedorIdDe(usuarioId);
        Map<Long, String> nombres = consultaUsuariosService.nombresClientes();

        Map<Orden, List<DetalleOrden>> porOrden = detallesConfirmados(vendedorId).stream()
                .collect(Collectors.groupingBy(DetalleOrden::getOrden, LinkedHashMap::new, Collectors.toList()));

        return porOrden.entrySet().stream()
                .filter(e -> estado == null || estado.isBlank() || e.getKey().getEstadoOrden().equalsIgnoreCase(estado))
                .map(e -> toDTO(e.getKey(), e.getValue(), nombres.get(e.getKey().getClienteId())))
                .toList();
    }

    @Transactional
    public OrdenVendedorDTO marcarPreparado(Long ordenId, Long usuarioId) {
        Long vendedorId = consultaUsuariosService.vendedorIdDe(usuarioId);
        List<DetalleOrden> propios = detalleOrdenRepository.findByOrdenId(ordenId).stream()
                .filter(d -> vendedorId.equals(d.getVendedorId()))
                .toList();
        if (propios.isEmpty()) {
            throw new SecurityException("La orden no contiene productos de su tienda");
        }
        Orden orden = propios.get(0).getOrden();
        if (!EstadoOrden.PAGADO.equals(orden.getEstadoOrden())) {
            throw new IllegalStateException("Solo se pueden preparar órdenes pagadas (estado actual " + orden.getEstadoOrden() + ")");
        }
        orden.setEstadoOrden(EstadoOrden.PREPARADO);
        ordenRepository.save(orden);
        log.info("Orden {} marcada como preparada por vendedor {}", ordenId, vendedorId);
        return toDTO(orden, propios, consultaUsuariosService.nombresClientes().get(orden.getClienteId()));
    }

    @Transactional(readOnly = true)
    public ResumenVendedorDTO getResumen(Long usuarioId) {
        Long vendedorId = consultaUsuariosService.vendedorIdDe(usuarioId);
        List<DetalleOrden> detalles = detallesConfirmados(vendedorId);
        LocalDate hoy = LocalDate.now();

        List<VentaDiariaDTO> semana = IntStream.rangeClosed(0, 6)
                .mapToObj(i -> hoy.minusDays(6 - i))
                .map(fecha -> {
                    List<DetalleOrden> delDia = filtrar(detalles, d -> d.getOrden().getFechaOrden().toLocalDate().equals(fecha));
                    return new VentaDiariaDTO(fecha, sumar(delDia),
                            delDia.stream().map(d -> d.getOrden().getId()).distinct().count());
                })
                .toList();

        return new ResumenVendedorDTO(
                semana.get(6).total(),
                semana.stream().map(VentaDiariaDTO::total).reduce(BigDecimal.ZERO, BigDecimal::add),
                detalles.stream()
                        .filter(d -> EstadoOrden.PAGADO.equals(d.getOrden().getEstadoOrden()))
                        .map(d -> d.getOrden().getId()).distinct().count(),
                detalles.stream().mapToLong(DetalleOrden::getCantidad).sum(),
                semana
        );
    }

    private List<DetalleOrden> detallesConfirmados(Long vendedorId) {
        return filtrar(detalleOrdenRepository.findByVendedorId(vendedorId),
                d -> EstadoOrden.VENTA_CONFIRMADA.contains(d.getOrden().getEstadoOrden()));
    }

    private List<DetalleOrden> filtrar(List<DetalleOrden> detalles, Predicate<DetalleOrden> filtro) {
        return detalles.stream().filter(filtro).toList();
    }

    private BigDecimal sumar(List<DetalleOrden> detalles) {
        return detalles.stream()
                .map(d -> d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private OrdenVendedorDTO toDTO(Orden o, List<DetalleOrden> propios, String nombreCliente) {
        return new OrdenVendedorDTO(
                o.getId(),
                o.getFechaOrden(),
                o.getEstadoOrden(),
                sumar(propios),
                nombreCliente,
                o.getDireccionEnvio(),
                o.getDistritoEnvio(),
                o.getLatitudEnvio(),
                o.getLongitudEnvio(),
                propios.stream().map(OrdenService::toItemDTO).toList()
        );
    }
}
