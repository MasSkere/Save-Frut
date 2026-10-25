package com.savefrut.msordenes.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "detalle_orden")
public class DetalleOrden {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "detalle_orden_seq")
    @SequenceGenerator(name = "detalle_orden_seq", sequenceName = "detalle_orden_detalle_orden_id_seq", allocationSize = 1)
    @Column(name = "detalle_orden_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_id", nullable = false)
    private Orden orden;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    /** Copia del nombre al momento de la compra (el catálogo vive en otra BD). */
    @Column(name = "nombre_producto", length = 100)
    private String nombreProducto;

    @Column(name = "vendedor_id")
    private Long vendedorId;

    @Column(name = "cantidad", nullable = false)
    private int cantidad;

    @Column(name = "precio_unitario", nullable = false)
    private BigDecimal precioUnitario;
}
