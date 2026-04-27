package com.savefrut.api.common.service;


import com.savefrut.api.common.dto.ComprobanteDTO;
import com.savefrut.api.common.dto.ItemDTO;
import com.savefrut.api.common.dto.OrdenRequestDTO;
import com.savefrut.api.common.entity.Cliente;
import com.savefrut.api.common.entity.DetalleOrden;
import com.savefrut.api.common.entity.Orden;
import com.savefrut.api.common.entity.Producto;
import com.savefrut.api.common.mapper.OrdenMapper;
import com.savefrut.api.common.repository.ClienteRepository;
import com.savefrut.api.common.repository.DetalleOrdenRepository;
import com.savefrut.api.common.repository.OrdenRepository;
import com.savefrut.api.common.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrdenService {

    private static final BigDecimal TARIFA_VARIEDAD       = new BigDecimal("5.00");
    private static final BigDecimal COSTO_ENVIO_DOMICILIO = new BigDecimal("10.00");
    private static final BigDecimal COSTO_ENVIO_RECOJO  = BigDecimal.ZERO;

    private final OrdenRepository        ordenRepository;
    private final ClienteRepository      clienteRepository;
    private final ProductoRepository     productoRepository;
    private final DetalleOrdenRepository detalleOrdenRepository;
    private final OrdenMapper            ordenMapper;

    @Transactional
    public Orden crearOrden(OrdenRequestDTO request, Long usuarioId) {
        Cliente cliente        = obtenerClienteValido(usuarioId);
        Map<Long, Producto> productosReales = obtenerYValidarProductos(request.getItems());

        validarStock(request.getItems(), productosReales);

        BigDecimal subtotal      = calcularSubtotal(request.getItems());
        BigDecimal tarifaVariedad = calcularTarifaVariedad(productosReales);
        BigDecimal costoEnvio   = resolverCostoEnvio(request.getCostoEnvio());
        BigDecimal total        = subtotal.add(tarifaVariedad).add(costoEnvio);

        Orden ordenGuardada = guardarCabeceraOrden(request, cliente, subtotal, costoEnvio, tarifaVariedad, total);
        guardarDetallesYActualizarStock(ordenGuardada.getId(), request.getItems(), productosReales);

        log.info("Orden {} creada para el cliente {}", ordenGuardada.getId(), usuarioId);
        return ordenGuardada;
    }

    @Transactional(readOnly = true)
    public ComprobanteDTO getDatosComprobante(Long ordenId, Long usuarioId) {
        Orden   orden   = obtenerOrdenExistente(ordenId);
        Cliente cliente = obtenerClienteValido(usuarioId);

        verificarPertenenciaOrden(orden, cliente);

        return ordenMapper.toComprobanteDTO(
                orden,
                cliente,
                detalleOrdenRepository.findByOrdenId(ordenId)
        );
    }

    private Cliente obtenerClienteValido(Long usuarioId) {
        return clienteRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Cliente no encontrado para el usuario: " + usuarioId));
    }

    private Orden obtenerOrdenExistente(Long ordenId) {
        return ordenRepository.findById(ordenId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Orden no encontrada: " + ordenId));
    }

    private Map<Long, Producto> obtenerYValidarProductos(List<ItemDTO> items) {
        Set<Long> ids = items.stream()
                .map(ItemDTO::getProductoId)
                .collect(Collectors.toSet());

        Map<Long, Producto> productosReales = StreamSupport
                .stream(productoRepository.findAllById(ids).spliterator(), false)
                .collect(Collectors.toMap(Producto::getId, p -> p));

        ids.forEach(id -> {
            if (!productosReales.containsKey(id)) {
                throw new IllegalArgumentException("Producto no encontrado: " + id);
            }
        });

        return productosReales;
    }

    private void validarStock(List<ItemDTO> items, Map<Long, Producto> productosReales) {
        items.forEach(item -> {
            Producto producto = productosReales.get(item.getProductoId());
            if (producto.getStock() < item.getCantidad()) {
                throw new IllegalStateException(
                        "Stock insuficiente para: " + producto.getNombre());
            }
        });
    }

    private void verificarPertenenciaOrden(Orden orden, Cliente cliente) {
        if (!orden.getCliente().getClienteId().equals(cliente.getClienteId())) {
            throw new SecurityException("Acceso denegado: esta orden no le pertenece.");
        }
    }

    private BigDecimal calcularSubtotal(List<ItemDTO> items) {
        return items.stream()
                .map(item -> item.getPrecioUnitario()
                        .multiply(BigDecimal.valueOf(item.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcularTarifaVariedad(Map<Long, Producto> productosReales) {
        long cantidadVendedores = productosReales.values().stream()
                .map(p -> p.getVendedor().getVendedorId())
                .distinct()
                .count();

        return cantidadVendedores > 1 ? TARIFA_VARIEDAD : BigDecimal.ZERO;
    }

    private BigDecimal resolverCostoEnvio(Double costoEnvioCliente) {
        BigDecimal costoEnvio = BigDecimal.valueOf(costoEnvioCliente);
        return COSTO_ENVIO_DOMICILIO.compareTo(costoEnvio) == 0
                ? COSTO_ENVIO_DOMICILIO
                : COSTO_ENVIO_RECOJO;
    }

    private Orden guardarCabeceraOrden(OrdenRequestDTO request, Cliente cliente,
                                     BigDecimal subtotal, BigDecimal costoEnvio,
                                     BigDecimal tarifaVariedad, BigDecimal total) {
        Orden orden = new Orden();
        orden.setCliente(cliente);
        orden.setFechaOrden(LocalDateTime.now());
        orden.setEstadoOrden(request.getMetodoPago());
        orden.setSubtotal(subtotal);
        orden.setCostoEnvio(costoEnvio);
        orden.setTarifaVariedad(tarifaVariedad);
        orden.setTotal(total);
        orden.setDireccionEnvio(request.getDireccionEnvio());
        orden.setDistritoEnvio(request.getDistritoNombre());
        return ordenRepository.save(orden);
    }

    private void guardarDetallesYActualizarStock(Long ordenId, List<ItemDTO> items, Map<Long, Producto> productosReales) {
        items.forEach(item -> {
            Producto producto = productosReales.get(item.getProductoId());

            DetalleOrden detalle = new DetalleOrden();
            detalle.setOrden(ordenRepository.findById(ordenId).orElseThrow());
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(item.getPrecioUnitario());
            detalleOrdenRepository.save(detalle);

            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);

        });
    }
}
