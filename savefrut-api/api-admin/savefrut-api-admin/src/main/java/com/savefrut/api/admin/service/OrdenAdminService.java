package com.savefrut.api.admin.service;

import com.savefrut.api.admin.dto.OrdenAdminDTO;
import com.savefrut.api.common.entity.DetalleOrden;
import com.savefrut.api.common.entity.Orden;
import com.savefrut.api.common.entity.Producto;
import com.savefrut.api.common.repository.DetalleOrdenRepository;
import com.savefrut.api.common.repository.OrdenRepository;
import com.savefrut.api.common.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrdenAdminService {

    private final OrdenRepository ordenRepository;
    private final DetalleOrdenRepository detalleOrdenRepository;
    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public List<OrdenAdminDTO> getOrdenes(String estado) {
        return estado == null
                ? ordenRepository.findAll().stream().map(this::toDTO).toList()
                : ordenRepository.findByEstado(estado).stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public OrdenAdminDTO getOrdenById(Long id) {
        return ordenRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));
    }

    @Transactional
    public OrdenAdminDTO cambiarEstado(Long id, String nuevoEstado) {
        return ordenRepository.findById(id)
                .map(orden -> {
                    log.info("Orden {} cambiada a {}", id, nuevoEstado);
                    orden.setEstadoOrden(nuevoEstado);
                    return toDTO(ordenRepository.save(orden));
                })
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));
    }

    @Transactional
    public OrdenAdminDTO cancelarOrden(Long id, String razon) {
        return ordenRepository.findById(id)
                .filter(o -> o.getEstadoOrden().equals("Pendiente"))
                .map(orden -> {
                    orden.setEstadoOrden("Cancelado");
                    ordenRepository.save(orden);

                    // Devolver stock
                    detalleOrdenRepository.findByOrdenId(id).forEach(detalle -> {
                        Producto p = detalle.getProducto();
                        p.setStock(p.getStock() + detalle.getCantidad());
                        productoRepository.save(p);
                    });

                    log.info("Orden {} cancelada. Razón: {}", id, razon);
                    return toDTO(orden);
                })
                .orElseThrow(() -> new IllegalArgumentException("No se puede cancelar esta orden"));
    }

    @Transactional(readOnly = true)
    public java.util.Map<String, Long> getEstadisticas() {
        return ordenRepository.findAll().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        Orden::getEstadoOrden,
                        java.util.stream.Collectors.counting()));
    }

    private OrdenAdminDTO toDTO(Orden o) {
        return new OrdenAdminDTO(
                o.getId(),
                o.getCliente().getClienteId(),
                o.getCliente().getNombres() + " " + o.getCliente().getApellidos(),
                o.getFechaOrden(),
                o.getEstadoOrden(),
                o.getSubtotal(),
                o.getCostoEnvio(),
                o.getTarifaVariedad(),
                o.getTotal(),
                o.getDireccionEnvio(),
                o.getDistritoEnvio()
        );
    }
}