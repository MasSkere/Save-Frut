package com.savefrut.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Entity
@Table(name = "ordenes")
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orden_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "fecha_orden", nullable = false, updatable = false)
    private Instant fechaOrden = Instant.now();

    @Column(name = "estado_orden", nullable = false, length = 50)
    private String estadoOrden;
    

    @Column(name = "subtotal", nullable = false)
    private BigDecimal subtotal;

    @Column(name = "costo_envio", nullable = false)
    private BigDecimal costoEnvio;

    @Column(name = "tarifa_variedad", nullable = false)
    private BigDecimal tarifaVariedad;

    @Column(name = "total", nullable = false)
    private BigDecimal total;

    @Column(name = "direccion_envio", nullable = false, length = 255)
    private String direccionEnvio;
    
    @Column(name = "distrito_envio", nullable = false, length = 100)
    private String distritoEnvio;
}