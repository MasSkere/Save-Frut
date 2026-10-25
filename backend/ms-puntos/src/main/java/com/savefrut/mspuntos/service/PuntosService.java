package com.savefrut.mspuntos.service;

import com.savefrut.mspuntos.dto.EstadisticasPuntosDTO;
import com.savefrut.mspuntos.dto.MisPuntosDTO;
import com.savefrut.mspuntos.entity.MovimientoPuntos;
import com.savefrut.mspuntos.entity.PuntosCliente;
import com.savefrut.mspuntos.event.OrdenPagadaEvent;
import com.savefrut.mspuntos.repository.MovimientoPuntosRepository;
import com.savefrut.mspuntos.repository.PuntosClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Programa de fidelización Eco-Puntos:
 * 1 punto por cada S/ 1 gastado + 5 puntos por cada fruta salvada (unidad comprada).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PuntosService {

    private static final int PUNTOS_POR_FRUTA = 5;

    private final PuntosClienteRepository puntosClienteRepository;
    private final MovimientoPuntosRepository movimientoPuntosRepository;

    @Transactional
    public void sumarPuntos(OrdenPagadaEvent evento) {
        if (evento.getClienteId() == null || evento.getOrdenId() == null) {
            log.warn("Evento OrdenPagadaEvent incompleto, se descarta: {}", evento);
            return;
        }
        if (movimientoPuntosRepository.existsByOrdenId(evento.getOrdenId())) {
            log.info("Orden {} ya acreditada, evento duplicado ignorado", evento.getOrdenId());
            return;
        }

        BigDecimal monto = evento.getTotal() == null ? BigDecimal.ZERO : evento.getTotal();
        int frutas = evento.getCantidadProductos() == null ? 0 : evento.getCantidadProductos();
        int puntos = calcularPuntos(monto, frutas);
        LocalDateTime fecha = evento.getFechaPago() == null ? LocalDateTime.now() : evento.getFechaPago();

        PuntosCliente cuenta = puntosClienteRepository.findByClienteId(evento.getClienteId())
                .orElseGet(() -> nuevaCuenta(evento.getClienteId()));
        cuenta.setPuntosAcumulados(cuenta.getPuntosAcumulados() + puntos);
        cuenta.setMontoTotalGastado(cuenta.getMontoTotalGastado().add(monto));
        cuenta.setFrutasSalvadas(cuenta.getFrutasSalvadas() + frutas);
        cuenta.setTotalCompras(cuenta.getTotalCompras() + 1);
        if (cuenta.getFechaPrimeraCompra() == null) {
            cuenta.setFechaPrimeraCompra(fecha);
        }
        cuenta.setFechaUltimaCompra(fecha);
        puntosClienteRepository.save(cuenta);

        MovimientoPuntos movimiento = new MovimientoPuntos();
        movimiento.setClienteId(evento.getClienteId());
        movimiento.setOrdenId(evento.getOrdenId());
        movimiento.setPuntos(puntos);
        movimiento.setMonto(monto);
        movimiento.setFrutasSalvadas(frutas);
        movimiento.setFecha(fecha);
        movimientoPuntosRepository.save(movimiento);

        log.info("Cliente {}: +{} Eco-Puntos por la orden {} ({} frutas salvadas)",
                evento.getClienteId(), puntos, evento.getOrdenId(), frutas);
    }

    @Transactional(readOnly = true)
    public MisPuntosDTO getMisPuntos(Long clienteId) {
        PuntosCliente cuenta = puntosClienteRepository.findByClienteId(clienteId)
                .orElseGet(() -> nuevaCuenta(clienteId));
        List<MisPuntosDTO.MovimientoDTO> movimientos = movimientoPuntosRepository
                .findTop20ByClienteIdOrderByFechaDesc(clienteId).stream()
                .map(m -> new MisPuntosDTO.MovimientoDTO(m.getOrdenId(), m.getPuntos(), m.getMonto(), m.getFrutasSalvadas(), m.getFecha()))
                .toList();
        Nivel nivel = Nivel.de(cuenta.getPuntosAcumulados());

        return new MisPuntosDTO(
                cuenta.getPuntosAcumulados(),
                cuenta.getMontoTotalGastado(),
                cuenta.getFrutasSalvadas(),
                cuenta.getTotalCompras(),
                nivel.nombre,
                nivel.siguiente == null ? null : nivel.siguiente.minimo - cuenta.getPuntosAcumulados(),
                movimientos
        );
    }

    /** Métricas para el Objetivo 2: tasa de retención = clientes con 2+ compras / clientes con al menos 1 compra. */
    @Transactional(readOnly = true)
    public EstadisticasPuntosDTO getEstadisticas() {
        long compradores = puntosClienteRepository.countByTotalComprasGreaterThan(0);
        long recurrentes = puntosClienteRepository.countByTotalComprasGreaterThan(1);
        BigDecimal tasa = compradores == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(recurrentes * 100.0 / compradores).setScale(1, RoundingMode.HALF_UP);
        List<EstadisticasPuntosDTO.RankingDTO> ranking = puntosClienteRepository.findTop10ByOrderByPuntosAcumuladosDesc().stream()
                .map(p -> new EstadisticasPuntosDTO.RankingDTO(p.getClienteId(), p.getPuntosAcumulados(), p.getFrutasSalvadas(), p.getTotalCompras()))
                .toList();
        return new EstadisticasPuntosDTO(compradores, recurrentes, tasa, puntosClienteRepository.sumFrutasSalvadas(), ranking);
    }

    private int calcularPuntos(BigDecimal monto, int frutas) {
        return monto.setScale(0, RoundingMode.DOWN).intValue() + frutas * PUNTOS_POR_FRUTA;
    }

    private PuntosCliente nuevaCuenta(Long clienteId) {
        PuntosCliente pc = new PuntosCliente();
        pc.setClienteId(clienteId);
        return pc;
    }

    private enum Nivel {
        GUARDIAN("Guardián del Planeta", 1000, null),
        HEROE("Héroe Verde", 300, GUARDIAN),
        BROTE("Brote", 100, HEROE),
        SEMILLA("Semilla", 0, BROTE);

        private final String nombre;
        private final int minimo;
        private final Nivel siguiente;

        Nivel(String nombre, int minimo, Nivel siguiente) {
            this.nombre = nombre;
            this.minimo = minimo;
            this.siguiente = siguiente;
        }

        static Nivel de(int puntos) {
            for (Nivel n : values()) {
                if (puntos >= n.minimo) {
                    return n;
                }
            }
            return SEMILLA;
        }
    }
}
