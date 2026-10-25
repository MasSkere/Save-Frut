package com.savefrut.msordenes.service;

import com.savefrut.msordenes.dto.ClienteResumenDTO;
import com.savefrut.msordenes.dto.DetalleItemDTO;
import com.savefrut.msordenes.dto.ItemDTO;
import com.savefrut.msordenes.dto.OrdenDTO;
import com.savefrut.msordenes.dto.OrdenRequestDTO;
import com.savefrut.msordenes.dto.ProductoCatalogoDTO;
import com.savefrut.msordenes.entity.DetalleOrden;
import com.savefrut.msordenes.entity.EstadoOrden;
import com.savefrut.msordenes.entity.Orden;
import com.savefrut.msordenes.feign.CatalogoFeignClient;
import com.savefrut.msordenes.repository.DetalleOrdenRepository;
import com.savefrut.msordenes.repository.OrdenRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrdenService {

    private static final BigDecimal TARIFA_VARIEDAD = new BigDecimal("5.00");
    private static final BigDecimal COSTO_ENVIO_DOMICILIO = new BigDecimal("10.00");

    private final OrdenRepository ordenRepository;
    private final DetalleOrdenRepository detalleOrdenRepository;
    private final CatalogoFeignClient catalogoFeignClient;
    private final ConsultaUsuariosService consultaUsuariosService;

    @Transactional
    public OrdenDTO crearOrden(OrdenRequestDTO request, Long usuarioId) {
        ClienteResumenDTO cliente = consultaUsuariosService.clienteDe(usuarioId);
        List<ItemDTO> items = agruparItems(request.getItems());
        Map<Long, ProductoCatalogoDTO> productos = obtenerProductos(items);

        validarStock(items, productos);

        BigDecimal subtotal = calcularSubtotal(items, productos);
        BigDecimal tarifaVariedad = calcularTarifaVariedad(productos);
        BigDecimal costoEnvio = "DOMICILIO".equals(request.getTipoEntrega()) ? COSTO_ENVIO_DOMICILIO : BigDecimal.ZERO;

        Orden orden = new Orden();
        orden.setClienteId(cliente.id());
        orden.setFechaOrden(LocalDateTime.now());
        orden.setEstadoOrden(EstadoOrden.PENDIENTE_PAGO);
        orden.setSubtotal(subtotal);
        orden.setCostoEnvio(costoEnvio);
        orden.setTarifaVariedad(tarifaVariedad);
        orden.setTotal(subtotal.add(tarifaVariedad).add(costoEnvio));
        orden.setDireccionEnvio(request.getDireccionEnvio());
        orden.setDistritoEnvio(request.getDistritoNombre());
        orden.setLatitudEnvio(request.getLatitud());
        orden.setLongitudEnvio(request.getLongitud());
        orden = ordenRepository.save(orden);

        reservarStock(items);
        List<DetalleOrden> detalles = guardarDetalles(orden, items, productos);

        log.info("Orden {} creada para el cliente {} por S/ {}", orden.getId(), cliente.id(), orden.getTotal());
        return toDTO(orden, detalles, cliente.nombreCompleto());
    }

    @Transactional(readOnly = true)
    public List<OrdenDTO> misOrdenes(Long usuarioId) {
        ClienteResumenDTO cliente = consultaUsuariosService.clienteDe(usuarioId);
        List<Orden> ordenes = ordenRepository.findByClienteIdOrderByFechaOrdenDesc(cliente.id());
        Map<Long, List<DetalleOrden>> detalles = detallesPorOrden(ordenes);
        return ordenes.stream()
                .map(o -> toDTO(o, detalles.getOrDefault(o.getId(), List.of()), cliente.nombreCompleto()))
                .toList();
    }

    @Transactional(readOnly = true)
    public OrdenDTO detalleParaCliente(Long ordenId, Long usuarioId) {
        ClienteResumenDTO cliente = consultaUsuariosService.clienteDe(usuarioId);
        Orden orden = obtenerOrden(ordenId);
        if (!orden.getClienteId().equals(cliente.id())) {
            throw new SecurityException("Acceso denegado: esta orden no le pertenece.");
        }
        return toDTO(orden, detalleOrdenRepository.findByOrdenId(ordenId), cliente.nombreCompleto());
    }

    public Orden obtenerOrden(Long ordenId) {
        return ordenRepository.findById(ordenId)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada: " + ordenId));
    }

    public Map<Long, List<DetalleOrden>> detallesPorOrden(List<Orden> ordenes) {
        if (ordenes.isEmpty()) {
            return Map.of();
        }
        return detalleOrdenRepository.findByOrdenIdIn(ordenes.stream().map(Orden::getId).toList()).stream()
                .collect(Collectors.groupingBy(d -> d.getOrden().getId()));
    }

    public void liberarStock(List<DetalleOrden> detalles) {
        detalles.forEach(d -> {
            try {
                catalogoFeignClient.liberarStock(d.getProductoId(), d.getCantidad());
            } catch (FeignException e) {
                log.error("No se pudo liberar stock del producto {}: {}", d.getProductoId(), e.getMessage());
            }
        });
    }

    public static OrdenDTO toDTO(Orden o, List<DetalleOrden> detalles, String nombreCliente) {
        return new OrdenDTO(
                o.getId(),
                o.getClienteId(),
                nombreCliente,
                o.getFechaOrden(),
                o.getEstadoOrden(),
                o.getSubtotal(),
                o.getCostoEnvio(),
                o.getTarifaVariedad(),
                o.getTotal(),
                o.getDireccionEnvio(),
                o.getDistritoEnvio(),
                o.getLatitudEnvio(),
                o.getLongitudEnvio(),
                o.getFechaPago(),
                detalles.stream().map(OrdenService::toItemDTO).toList()
        );
    }

    public static DetalleItemDTO toItemDTO(DetalleOrden d) {
        return new DetalleItemDTO(
                d.getProductoId(),
                d.getNombreProducto(),
                d.getVendedorId(),
                d.getCantidad(),
                d.getPrecioUnitario(),
                d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad()))
        );
    }

    /** Une líneas repetidas del mismo producto para reservar el stock una sola vez. */
    private List<ItemDTO> agruparItems(List<ItemDTO> items) {
        return items.stream()
                .collect(Collectors.toMap(ItemDTO::getProductoId, Function.identity(), (a, b) -> {
                    ItemDTO suma = new ItemDTO();
                    suma.setProductoId(a.getProductoId());
                    suma.setCantidad(a.getCantidad() + b.getCantidad());
                    return suma;
                }))
                .values().stream().toList();
    }

    private Map<Long, ProductoCatalogoDTO> obtenerProductos(List<ItemDTO> items) {
        return items.stream()
                .map(item -> obtenerProducto(item.getProductoId()))
                .collect(Collectors.toMap(ProductoCatalogoDTO::getId, Function.identity()));
    }

    private ProductoCatalogoDTO obtenerProducto(Long productoId) {
        try {
            return catalogoFeignClient.obtenerProducto(productoId);
        } catch (FeignException.NotFound e) {
            throw new IllegalArgumentException("Producto no disponible: " + productoId);
        }
    }

    private void validarStock(List<ItemDTO> items, Map<Long, ProductoCatalogoDTO> productos) {
        items.forEach(item -> {
            if (!Boolean.TRUE.equals(catalogoFeignClient.verificarStock(item.getProductoId(), item.getCantidad()))) {
                throw new IllegalStateException("Stock insuficiente para: " + productos.get(item.getProductoId()).getNombre());
            }
        });
    }

    /** El precio se toma del catálogo (descuento dinámico vigente), nunca del cliente. */
    private BigDecimal calcularSubtotal(List<ItemDTO> items, Map<Long, ProductoCatalogoDTO> productos) {
        return items.stream()
                .map(item -> productos.get(item.getProductoId()).getPrecioDescuento()
                        .multiply(BigDecimal.valueOf(item.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcularTarifaVariedad(Map<Long, ProductoCatalogoDTO> productos) {
        long cantidadVendedores = productos.values().stream()
                .map(ProductoCatalogoDTO::getVendedorId)
                .distinct()
                .count();
        return cantidadVendedores > 1 ? TARIFA_VARIEDAD : BigDecimal.ZERO;
    }

    private void reservarStock(List<ItemDTO> items) {
        List<ItemDTO> reservados = new ArrayList<>();
        for (ItemDTO item : items) {
            if (!Boolean.TRUE.equals(catalogoFeignClient.reservarStock(item.getProductoId(), item.getCantidad()))) {
                reservados.forEach(r -> catalogoFeignClient.liberarStock(r.getProductoId(), r.getCantidad()));
                throw new IllegalStateException("El stock del producto se agotó durante el procesamiento");
            }
            reservados.add(item);
        }
    }

    private List<DetalleOrden> guardarDetalles(Orden orden, List<ItemDTO> items, Map<Long, ProductoCatalogoDTO> productos) {
        return items.stream()
                .map(item -> {
                    ProductoCatalogoDTO producto = productos.get(item.getProductoId());
                    DetalleOrden detalle = new DetalleOrden();
                    detalle.setOrden(orden);
                    detalle.setProductoId(producto.getId());
                    detalle.setNombreProducto(producto.getNombre());
                    detalle.setVendedorId(producto.getVendedorId());
                    detalle.setCantidad(item.getCantidad());
                    detalle.setPrecioUnitario(producto.getPrecioDescuento());
                    return detalleOrdenRepository.save(detalle);
                })
                .toList();
    }
}
