package com.savefrut.api.common.mapper;

import com.savefrut.api.common.dto.ComprobanteDTO;
import com.savefrut.api.common.dto.DetalleItemDTO;
import com.savefrut.api.common.entity.Cliente;
import com.savefrut.api.common.entity.DetalleOrden;
import com.savefrut.api.common.entity.Orden;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper para convertir entidades de orden a DTOs de respuesta.
 *
 * <p>MapStruct genera la implementación en tiempo de compilación —
 * no hay reflection ni lógica manual.
 */
@Mapper(componentModel = "spring")
public interface OrdenMapper {

    /**
     * Convierte una orden, su cliente y sus detalles en un comprobante de compra.
     *
     * @param orden    cabecera de la orden
     * @param cliente  cliente que realizó la compra
     * @param detalles lista de ítems de la orden
     * @return comprobante listo para enviar al frontend
     */
    @Mapping(target = "ordenId",        source = "orden.id")
    @Mapping(target = "fechaOrden",     source = "orden.fechaOrden")
    @Mapping(target = "estadoOrden",    source = "orden.estadoOrden")
    @Mapping(target = "direccionEnvio", source = "orden.direccionEnvio")
    @Mapping(target = "distritoEnvio",  source = "orden.distritoEnvio")
    @Mapping(target = "subtotal",       source = "orden.subtotal")
    @Mapping(target = "costoEnvio",     source = "orden.costoEnvio")
    @Mapping(target = "tarifaVariedad", source = "orden.tarifaVariedad")
    @Mapping(target = "total",          source = "orden.total")
    @Mapping(target = "clienteNombres", expression = "java(cliente.getNombres() + \" \" + cliente.getApellidos())")
    @Mapping(target = "clienteEmail",   source = "cliente.usuario.email")
    @Mapping(target = "items",          source = "detalles")
    ComprobanteDTO toComprobanteDTO(Orden orden, Cliente cliente, List<DetalleOrden> detalles);

    /**
     * Convierte un detalle de orden en un ítem del comprobante.
     */
    @Mapping(target = "nombreProducto", source = "producto.nombre")
    @Mapping(target = "cantidad",       source = "cantidad")
    @Mapping(target = "precioUnitario", source = "precioUnitario")
    @Mapping(target = "subtotalItem",   expression = "java(detalle.getPrecioUnitario().multiply(java.math.BigDecimal.valueOf(detalle.getCantidad())))")
    DetalleItemDTO toDetalleItemDTO(DetalleOrden detalle);
}