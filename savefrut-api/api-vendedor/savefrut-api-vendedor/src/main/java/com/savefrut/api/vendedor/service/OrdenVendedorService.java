package com.savefrut.api.vendedor.service;

import com.savefrut.api.vendedor.dto.*;
import com.savefrut.api.common.entity.*;
import com.savefrut.api.common.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrdenVendedorService {

    private final OrdenRepository ordenRepository;
    private final DetalleOrdenRepository detalleOrdenRepository;
    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public List<OrdenVendedorDTO> getOrdenes(Long vendedorId, String estado) {
        List<DetalleOrden> detalles = detalleOrdenRepository.findByVendedorId(vendedorId);

        return detalles.stream()
                .map(DetalleOrden::getOrden)
                .distinct()
                .filter(o -> estado == null || o.getEstadoOrden().equals(estado))
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrdenVendedorDTO getOrdenById(Long ordenId, Long vendedorId) {
        return ordenRepository.findById(ordenId)
                .filter(orden -> detalleOrdenRepository.existsByOrdenIdAndVendedorId(ordenId, vendedorId))
                .map(this::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));
    }

    @Transactional
    public OrdenVendedorDTO marcarPreparado(Long ordenId, Long vendedorId) {
        return ordenRepository.findById(ordenId)
                .filter(orden -> detalleOrdenRepository.existsByOrdenIdAndVendedorId(ordenId, vendedorId))
                .map(orden -> {
                    orden.setEstadoOrden("Preparado");
                    ordenRepository.save(orden);
                    log.info("Orden {} marcada como preparada por vendedor {}", ordenId, vendedorId);
                    return toDTO(orden);
                })
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));
    }

    private OrdenVendedorDTO toDTO(Orden o) {
        Cliente cliente = o.getCliente();

        List<ItemVendedorDTO> items = detalleOrdenRepository.findByOrdenId(o.getId()).stream()
                .filter(d -> d.getProducto().getVendedor().getVendedorId().equals(0L)) // se corrige en repo
                .map(d -> new ItemVendedorDTO(
                        d.getId(),
                        d.getProducto().getNombre(),
                        d.getCantidad(),
                        d.getPrecioUnitario(),
                        d.getPrecioUnitario().multiply(java.math.BigDecimal.valueOf(d.getCantidad()))
                ))
                .toList();

        return new OrdenVendedorDTO(
                o.getId(),
                o.getFechaOrden(),
                o.getEstadoOrden(),
                o.getTotal(),
                cliente.getNombres() + " " + cliente.getApellidos(),
                o.getDireccionEnvio(),
                o.getDistritoEnvio(),
                items
        );
    }
}