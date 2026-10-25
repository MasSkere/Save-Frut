package com.savefrut.msordenes.controller;

import com.savefrut.msordenes.dto.ConfirmarPagoRequest;
import com.savefrut.msordenes.dto.OrdenDTO;
import com.savefrut.msordenes.dto.PreferenciaPagoDTO;
import com.savefrut.msordenes.service.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    /** Crea la preferencia de Checkout Pro. Requiere Idempotency-Key para no generar cobros duplicados. */
    @PostMapping("/preferencia/{ordenId}")
    @PreAuthorize("hasRole('CLIENTE')")
    public PreferenciaPagoDTO crearPreferencia(@PathVariable Long ordenId,
                                               @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
                                               @AuthenticationPrincipal Long usuarioId) {
        return pagoService.crearPreferencia(ordenId, usuarioId, idempotencyKey);
    }

    /** Confirma el pago cuando Mercado Pago redirige al usuario de vuelta a la tienda. */
    @PostMapping("/confirmar")
    @PreAuthorize("hasRole('CLIENTE')")
    public OrdenDTO confirmarPago(@Valid @RequestBody ConfirmarPagoRequest request,
                                  @AuthenticationPrincipal Long usuarioId) {
        return pagoService.confirmarPago(request.ordenId(), request.paymentId(), usuarioId);
    }

    /** "Ya pagué": consulta a Mercado Pago si la orden tiene un pago aprobado. */
    @PostMapping("/verificar/{ordenId}")
    @PreAuthorize("hasRole('CLIENTE')")
    public OrdenDTO verificarPago(@PathVariable Long ordenId, @AuthenticationPrincipal Long usuarioId) {
        return pagoService.verificarPago(ordenId, usuarioId);
    }

    /** Notificaciones IPN/Webhook de Mercado Pago (requiere URL pública configurada en MERCADO_PAGO_WEBHOOK_URL). */
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestParam Map<String, String> params,
                                        @RequestBody(required = false) Map<String, Object> body) {
        String tipo = params.getOrDefault("type", params.get("topic"));
        String paymentId = params.getOrDefault("data.id", params.get("id"));
        if (paymentId == null && body != null && body.get("data") instanceof Map<?, ?> data) {
            paymentId = String.valueOf(data.get("id"));
        }
        if ("payment".equals(tipo) && paymentId != null) {
            try {
                pagoService.procesarWebhook(paymentId);
            } catch (RuntimeException e) {
                log.error("Error procesando webhook del pago {}: {}", paymentId, e.getMessage());
            }
        }
        return ResponseEntity.ok().build();
    }
}
