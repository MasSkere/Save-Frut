package com.savefrut.api.controller;

import com.savefrut.api.dto.payment.PaymentIntentRequest;
import com.savefrut.api.dto.payment.PaymentIntentResponse;
import com.savefrut.api.service.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pagos")
@CrossOrigin
public class PaymentController {

    // ¡El controlador ahora solo habla con el servicio!
    @Autowired
    private PaymentService paymentService;

    /**
     * Endpoint para crear un intento de pago.
     * El frontend (js/pago.js) llama a esto.
     */
    @PostMapping("/crear-intento-pago")
    public ResponseEntity<PaymentIntentResponse> crearIntentoDePago(@RequestBody PaymentIntentRequest request) {

        try {
            // 1. Llamamos al servicio para que haga el trabajo
            PaymentIntent paymentIntent = paymentService.crearIntentoDePagoStripe(request);

            // 2. Si todo sale bien, devolvemos el clientSecret al frontend
            return ResponseEntity.ok(
                new PaymentIntentResponse(paymentIntent.getClientSecret(), paymentIntent.getAmount())
            );

        } catch (StripeException e) {
            // Error de Stripe (ej. clave secreta incorrecta)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PaymentIntentResponse(e.getMessage(), 0L));
        } catch (Exception e) {
            // Error nuestro (ej. Producto no encontrado al recalcular el total)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new PaymentIntentResponse(e.getMessage(), 0L));
        }
    }
}