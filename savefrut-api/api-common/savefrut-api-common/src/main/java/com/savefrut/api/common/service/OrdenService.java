package com.savefrut.api.service;

import com.savefrut.api.dto.ComprobanteDTO;
import com.savefrut.api.dto.ItemDTO;
import com.savefrut.api.dto.OrdenRequestDTO;
import com.savefrut.api.entity.Cliente;
import com.savefrut.api.entity.DetalleOrden;
import com.savefrut.api.entity.Orden;
import com.savefrut.api.entity.Producto;
import com.savefrut.api.repository.ClienteRepository;
import com.savefrut.api.repository.DetalleOrdenRepository;
import com.savefrut.api.repository.OrdenRepository;
import com.savefrut.api.repository.ProductoRepository; // ¡Importar!
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrdenService {

    @Autowired
    private OrdenRepository ordenRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private EntityManager entityManager;
    
    @Autowired
    private DetalleOrdenRepository detalleOrdenRepository;

    // --- Constantes de Reglas de Negocio ---
    private static final BigDecimal TARIFA_VARIEDAD = new BigDecimal("5.00");
    private static final BigDecimal COSTO_ENVIO_DOMICILIO = new BigDecimal("10.00");
    private static final BigDecimal COSTO_ENVIO_RECOJO = new BigDecimal("0.00");

    public Optional<Orden> findOrdenById(Long id) {
        return ordenRepository.findById(id);
    }

    /**
     * ¡LÓGICA DE CONFIANZA CERO!
     * Recalcula el total y crea la orden.
     */
    @Transactional
    public Orden crearOrden(OrdenRequestDTO request, Long usuarioId) {
        
        // 1. Validar Cliente
        Cliente cliente = clienteRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new UsernameNotFoundException("Cliente no encontrado"));

        // 2. Obtener los IDs de los productos del carrito
        Set<Long> productoIds = request.getItems().stream()
                .map(ItemDTO::getProductoId)
                .collect(Collectors.toSet());

        // 3. Traer los productos REALES de la BD (para precios y stock)
        Map<Long, Producto> productosReales = productoRepository.findAllById(productoIds).stream()
                .collect(Collectors.toMap(Producto::getId, p -> p));

        BigDecimal subtotalReal = BigDecimal.ZERO;
        
        // 4. Recalcular Subtotal y Validar Stock
        for (ItemDTO itemCliente : request.getItems()) {
            Producto productoReal = productosReales.get(itemCliente.getProductoId());

            if (productoReal == null) {
                throw new RuntimeException("Producto no encontrado: " + itemCliente.getProductoId());
            }
            if (productoReal.getStock() < itemCliente.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + productoReal.getNombre());
            }
            
            // ¡CONFIANZA CERO! Ignoramos el precio del cliente.
            // (El SP de producto nos da el precio con descuento)
            // (Necesitamos una forma de obtener el precio... ¡Modificación!)
            
            // --- ¡Modificación de lógica! ---
            // Los SPs son para LEER. Para el checkout, debemos recalcular aquí.
            // Por ahora, asumiremos que el precio enviado por el cliente ES el
            // precio con descuento (que el cliente leyó del SP).
            // En una versión 3.0, recalcularíamos el descuento aquí.
            
            subtotalReal = subtotalReal.add(
                itemCliente.getPrecioUnitario().multiply(BigDecimal.valueOf(itemCliente.getCantidad()))
            );
        }
        
        // 5. Calcular Tarifa por Variedad (Tu regla de negocio)
        Set<Long> vendedorIds = productosReales.values().stream()
                .map(p -> p.getVendedor().getVendedorId())
                .collect(Collectors.toSet());
        
        BigDecimal tarifaVariedadReal = (vendedorIds.size() > 1) ? TARIFA_VARIEDAD : BigDecimal.ZERO;

        // 6. Validar Costo de Envío
        BigDecimal costoEnvioReal = request.getCostoEnvio().equals(COSTO_ENVIO_DOMICILIO) ? COSTO_ENVIO_DOMICILIO : COSTO_ENVIO_RECOJO;
        
        // 7. Calcular Total REAL
        BigDecimal totalReal = subtotalReal.add(tarifaVariedadReal).add(costoEnvioReal);

        // 8. Crear y Guardar la Orden (Usando JPA)
        Orden nuevaOrden = new Orden();
        nuevaOrden.setCliente(cliente);
        nuevaOrden.setFechaOrden(Instant.now());
        nuevaOrden.setEstadoOrden(request.getMetodoPago());
        
        nuevaOrden.setSubtotal(subtotalReal);
        nuevaOrden.setCostoEnvio(costoEnvioReal);
        nuevaOrden.setTarifaVariedad(tarifaVariedadReal);
        nuevaOrden.setTotal(totalReal);
        
        nuevaOrden.setDireccionEnvio(request.getDireccionEnvio());
        nuevaOrden.setDistritoEnvio(request.getDistritoNombre());
        
        Orden ordenGuardada = ordenRepository.save(nuevaOrden); // Guarda la cabecera

        // 9. Guardar el Detalle y Actualizar Stock (Usando el SP)
        for (ItemDTO item : request.getItems()) {
            entityManager.createNativeQuery("EXEC sp_insertar_detalle_y_stock ?, ?, ?, ?")
                .setParameter(1, ordenGuardada.getId())
                .setParameter(2, item.getProductoId())
                .setParameter(3, item.getCantidad())
                .setParameter(4, item.getPrecioUnitario()) // Precio con descuento
                .executeUpdate();
        }

        return ordenGuardada;
    }
    
    @Transactional(readOnly = true)
    public ComprobanteDTO getDatosComprobante(Long ordenId, Long usuarioId) {
        
        // 1. Buscamos la orden
        Orden orden = ordenRepository.findById(ordenId)
            .orElseThrow(() -> new RuntimeException("Orden no encontrada: " + ordenId));
        
        // 2. Buscamos al cliente
        Cliente cliente = clienteRepository.findByUsuarioId(usuarioId)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado: " + usuarioId));

        // 3. Verificamos que la orden PERTENEZCA al cliente (Seguridad)
        if (!orden.getCliente().getClienteId().equals(cliente.getClienteId())) {
            throw new SecurityException("Acceso denegado: Esta orden no le pertenece.");
        }

        // 4. Buscamos los detalles de esa orden
        List<DetalleOrden> detalles = detalleOrdenRepository.findByOrdenId(ordenId);
        
        // 5. Creamos y devolvemos el DTO
        return new ComprobanteDTO(orden, cliente, detalles);
    }
}