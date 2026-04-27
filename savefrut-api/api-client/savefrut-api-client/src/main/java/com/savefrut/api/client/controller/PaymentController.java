package com.savefrut.api.client.controller;

import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import com.savefrut.api.common.dto.payment.PaymentIntentRequest;
import com.savefrut.api.common.dto.payment.PaymentIntentResponse;
import com.savefrut.api.common.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para el proceso de pago con Mercado Pago.
 *
 * <p>Responsabilidad única: recibir el request, delegar al servicio
 * y traducir el resultado a una respuesta HTTP.
 */
@Slf4j
@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Crea una preferencia de pago en Mercado Pago y devuelve
     * el {@code initPoint} para redirigir al usuario al checkout.
     *
     * @param request ítems del carrito y costo de envío
     * @return URL de pago de MP o mensaje de error
     */
    @PostMapping("/crear-intento-pago")
    public ResponseEntity<PaymentIntentResponse> crearIntentoDePago(
            @Valid @RequestBody PaymentIntentRequest request) {
        try {
            // ✅ nombre de método actualizado
            Preference preference = paymentService.crearPreferenciaDePago(request);
            return ResponseEntity.ok(buildRespuestaExitosa(preference));

        } catch (MPApiException e) {
            log.error("Error de API Mercado Pago: status={}, mensaje={}", e.getStatusCode(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(buildRespuestaError(e.getMessage()));

        } catch (MPException e) {
            log.error("Error de SDK Mercado Pago: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildRespuestaError(e.getMessage()));

        } catch (IllegalArgumentException e) {
            log.warn("Solicitud inválida al crear pago: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(buildRespuestaError(e.getMessage()));
        }
    }

    // ─── Helpers de respuesta ──────────────────────────────────────────────────

    private PaymentIntentResponse buildRespuestaExitosa(Preference preference) {
        long montoEnCentavos = preference.getItems().get(0).getUnitPrice()
                .multiply(new java.math.BigDecimal("100"))
                .longValue();
        return new PaymentIntentResponse(preference.getInitPoint(), montoEnCentavos);
    }

    private PaymentIntentResponse buildRespuestaError(String mensaje) {
        return new PaymentIntentResponse(mensaje, 0L);
    }
}