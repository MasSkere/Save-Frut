package com.savefrut.api.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Data;

// Esto es lo que le devolvemos al cliente:
// el "client_secret" de Stripe y el monto real que calculamos.
@Data
@AllArgsConstructor
public class PaymentIntentResponse {
    private String clientSecret;
    private Long amount; // El monto real calculado en centavos
}
