package com.savefrut.mspuntos.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "puntos_cliente")
public class PuntosCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "puntos_seq")
    @SequenceGenerator(name = "puntos_seq", sequenceName = "puntos_cliente_puntos_id_seq", allocationSize = 1)
    @Column(name = "puntos_id")
    private Long id;

    @Column(name = "cliente_id", nullable = false, unique = true)
    private Long clienteId;

    @Column(name = "puntos_acumulados", nullable = false)
    private Integer puntosAcumulados = 0;

    @Column(name = "monto_total_gastado", nullable = false)
    private BigDecimal montoTotalGastado = BigDecimal.ZERO;

    @Column(name = "frutas_salvadas", nullable = false)
    private Integer frutasSalvadas = 0;

    @Column(name = "total_compras", nullable = false)
    private Integer totalCompras = 0;

    @Column(name = "fecha_primera_compra")
    private LocalDateTime fechaPrimeraCompra;

    @Column(name = "fecha_ultima_compra")
    private LocalDateTime fechaUltimaCompra;
}
