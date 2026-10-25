package com.savefrut.msordenes.service;

import com.savefrut.msordenes.entity.EstadoOrden;
import com.savefrut.msordenes.entity.Orden;
import com.savefrut.msordenes.repository.OrdenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Conciliación periódica con Mercado Pago para órdenes con preferencia creada y aún sin pago,
 * por si el usuario no vuelve a la tienda tras pagar y no hay webhook disponible.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConciliacionPagosJob {

    private final OrdenRepository ordenRepository;
    private final PagoService pagoService;

    @Value("${mercadopago.access-token:}")
    private String accessToken;

    @Scheduled(fixedDelayString = "${mercadopago.conciliacion-ms:60000}", initialDelay = 30000)
    public void conciliarPendientes() {
        if (!StringUtils.hasText(accessToken)) {
            return;
        }
        List<Orden> pendientes = ordenRepository.findByEstadoOrdenAndMpPreferenceIdIsNotNullAndFechaOrdenAfter(
                EstadoOrden.PENDIENTE_PAGO, LocalDateTime.now().minusDays(2));
        for (Orden orden : pendientes) {
            try {
                if (pagoService.conciliar(orden.getId())) {
                    log.info("Conciliación: orden {} confirmada como pagada", orden.getId());
                }
            } catch (RuntimeException e) {
                log.warn("Conciliación de la orden {} omitida: {}", orden.getId(), e.getMessage());
                return;
            }
        }
    }
}
