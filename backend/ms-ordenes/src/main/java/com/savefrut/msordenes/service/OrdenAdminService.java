package com.savefrut.msordenes.service;

import com.savefrut.msordenes.dto.OrdenDTO;
import com.savefrut.msordenes.entity.DetalleOrden;
import com.savefrut.msordenes.entity.EstadoOrden;
import com.savefrut.msordenes.entity.Orden;
import com.savefrut.msordenes.repository.DetalleOrdenRepository;
import com.savefrut.msordenes.repository.OrdenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrdenAdminService {

    private final OrdenRepository ordenRepository;
    private final DetalleOrdenRepository detalleOrdenRepository;
    private final OrdenService ordenService;
    private final ConsultaUsuariosService consultaUsuariosService;

    @Transactional(readOnly = true)
    public List<OrdenDTO> getOrdenes(String estado) {
        List<Orden> ordenes = estado == null || estado.isBlank()
                ? ordenRepository.findAllByOrderByFechaOrdenDesc()
                : ordenRepository.findByEstadoOrdenOrderByFechaOrdenDesc(estado.toUpperCase());
        Map<Long, List<DetalleOrden>> detalles = ordenService.detallesPorOrden(ordenes);
        Map<Long, String> nombres = consultaUsuariosService.nombresClientes();
        return ordenes.stream()
                .map(o -> OrdenService.toDTO(o, detalles.getOrDefault(o.getId(), List.of()), nombres.get(o.getClienteId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public OrdenDTO getOrdenById(Long id) {
        Orden orden = ordenService.obtenerOrden(id);
        return OrdenService.toDTO(orden, detalleOrdenRepository.findByOrdenId(id),
                consultaUsuariosService.nombresClientes().get(orden.getClienteId()));
    }

    @Transactional
    public OrdenDTO cambiarEstado(Long id, String nuevoEstado) {
        String estado = nuevoEstado.toUpperCase();
        if (!EstadoOrden.TODOS.contains(estado)) {
            throw new IllegalArgumentException("Estado inválido. Valores permitidos: " + EstadoOrden.TODOS);
        }
        if (EstadoOrden.CANCELADO.equals(estado)) {
            return cancelarOrden(id, "Cambio de estado por administrador");
        }
        Orden orden = ordenService.obtenerOrden(id);
        orden.setEstadoOrden(estado);
        ordenRepository.save(orden);
        log.info("Orden {} cambiada a {}", id, estado);
        return getOrdenById(id);
    }

    @Transactional
    public OrdenDTO cancelarOrden(Long id, String razon) {
        Orden orden = ordenService.obtenerOrden(id);
        if (EstadoOrden.CANCELADO.equals(orden.getEstadoOrden()) || EstadoOrden.ENTREGADO.equals(orden.getEstadoOrden())) {
            throw new IllegalStateException("No se puede cancelar una orden en estado " + orden.getEstadoOrden());
        }
        orden.setEstadoOrden(EstadoOrden.CANCELADO);
        ordenRepository.save(orden);
        ordenService.liberarStock(detalleOrdenRepository.findByOrdenId(id));
        log.info("Orden {} cancelada. Razón: {}", id, razon);
        return getOrdenById(id);
    }
}
