package com.savefrut.api.dto;

import com.savefrut.api.entity.Cliente;
import com.savefrut.api.entity.DetalleOrden;
import com.savefrut.api.entity.Orden;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * DTO para enviar TODOS los datos al comprobante
 */
@Data
public class ComprobanteDTO {

    // Datos de la Orden
    private Long ordenId;
    private Instant fechaOrden;
    private String estadoOrden;
    private String direccionEnvio;
    private String distritoEnvio;
    
    // Datos del Cliente
    private String clienteNombres;
    private String clienteEmail;

    // Totales
    private BigDecimal subtotal;
    private BigDecimal costoEnvio;
    private BigDecimal tarifaVariedad;
    private BigDecimal total;

    // Items
    private List<DetalleItemDTO> items;

    // Constructor (convertimos las Entidades a DTO)
    public ComprobanteDTO(Orden orden, Cliente cliente, List<DetalleOrden> detalles) {
        this.ordenId = orden.getId();
        this.fechaOrden = orden.getFechaOrden();
        this.estadoOrden = orden.getEstadoOrden();
        this.direccionEnvio = orden.getDireccionEnvio();
        this.distritoEnvio = orden.getDistritoEnvio();
        
        this.clienteNombres = cliente.getNombres() + " " + cliente.getApellidos();
        this.clienteEmail = cliente.getUsuario().getEmail();

        this.subtotal = orden.getSubtotal();
        this.costoEnvio = orden.getCostoEnvio();
        this.tarifaVariedad = orden.getTarifaVariedad();
        this.total = orden.getTotal();
        
        // (Esto es avanzado, pero es la forma correcta)
        this.items = detalles.stream()
            .map(DetalleItemDTO::new)
            .collect(java.util.stream.Collectors.toList());
    }
}

// DTO interno para los items
@Data
class DetalleItemDTO {
    private String nombreProducto;
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotalItem;

    public DetalleItemDTO(DetalleOrden detalle) {
        this.nombreProducto = detalle.getProducto().getNombre(); // Asumimos que la relación está cargada
        this.cantidad = detalle.getCantidad();
        this.precioUnitario = detalle.getPrecioUnitario();
        this.subtotalItem = this.precioUnitario.multiply(new BigDecimal(this.cantidad));
    }
}