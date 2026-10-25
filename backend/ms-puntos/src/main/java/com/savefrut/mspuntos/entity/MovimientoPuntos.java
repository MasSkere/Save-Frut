package com.savefrut.mspuntos.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "movimientos_puntos")
public class MovimientoPuntos {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "movimiento_seq")
    @SequenceGenerator(name = "movimiento_seq", sequenceName = "movimientos_puntos_movimiento_id_seq", allocationSize = 1)
    @Column(name = "movimiento_id")
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "orden_id", nullable = false, unique = true)
    private Long ordenId;

    @Column(name = "puntos", nullable = false)
    private Integer puntos;

    @Column(name = "monto", nullable = false)
    private BigDecimal monto;

    @Column(name = "frutas_salvadas", nullable = false)
    private Integer frutasSalvadas;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;
}
