package com.savefrut.msordenes.service;

import com.savefrut.msordenes.exception.PagoNoDisponibleException;
import com.savefrut.msordenes.feign.MercadoPagoClient;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Acceso a la pasarela externa protegido con Circuit Breaker (Resilience4j).
 * Si Mercado Pago falla o responde lento, el circuito se abre y se responde con un fallback controlado.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MercadoPagoGateway {

    private static final String CIRCUIT = "mercadoPago";
    private static final String MENSAJE_FALLBACK =
            "La pasarela de pagos no está disponible en este momento. Tu orden quedó reservada, intenta pagar en unos minutos.";

    private final MercadoPagoClient mercadoPagoClient;

    @CircuitBreaker(name = CIRCUIT, fallbackMethod = "crearPreferenciaFallback")
    public Map<String, Object> crearPreferencia(String idempotencyKey, Map<String, Object> preferencia) {
        return mercadoPagoClient.crearPreferencia(idempotencyKey, preferencia);
    }

    @CircuitBreaker(name = CIRCUIT, fallbackMethod = "buscarPagosFallback")
    public Map<String, Object> buscarPagos(String externalReference) {
        return mercadoPagoClient.buscarPagos(externalReference);
    }

    private Map<String, Object> buscarPagosFallback(String externalReference, FeignException e) {
        log.error("Mercado Pago respondió con error al buscar pagos de la orden {}: status={}", externalReference, e.status());
        throw new PagoNoDisponibleException(MENSAJE_FALLBACK);
    }

    private Map<String, Object> buscarPagosFallback(String externalReference, CallNotPermittedException e) {
        log.warn("Circuit Breaker ABIERTO para Mercado Pago, se omite la conciliación de la orden {}", externalReference);
        throw new PagoNoDisponibleException(MENSAJE_FALLBACK);
    }

    @CircuitBreaker(name = CIRCUIT, fallbackMethod = "obtenerPagoFallback")
    public Map<String, Object> obtenerPago(String paymentId) {
        return mercadoPagoClient.obtenerPago(paymentId);
    }

    private Map<String, Object> crearPreferenciaFallback(String idempotencyKey, Map<String, Object> preferencia,
                                                         FeignException e) {
        log.error("Mercado Pago respondió con error al crear preferencia: status={} {}", e.status(), e.getMessage());
        throw new PagoNoDisponibleException(MENSAJE_FALLBACK);
    }

    private Map<String, Object> crearPreferenciaFallback(String idempotencyKey, Map<String, Object> preferencia,
                                                         CallNotPermittedException e) {
        log.warn("Circuit Breaker ABIERTO para Mercado Pago, se rechaza la creación de preferencia");
        throw new PagoNoDisponibleException(MENSAJE_FALLBACK);
    }

    private Map<String, Object> obtenerPagoFallback(String paymentId, FeignException e) {
        log.error("Mercado Pago respondió con error al consultar pago {}: status={}", paymentId, e.status());
        throw new PagoNoDisponibleException(MENSAJE_FALLBACK);
    }

    private Map<String, Object> obtenerPagoFallback(String paymentId, CallNotPermittedException e) {
        log.warn("Circuit Breaker ABIERTO para Mercado Pago, se rechaza la consulta del pago {}", paymentId);
        throw new PagoNoDisponibleException(MENSAJE_FALLBACK);
    }
}
