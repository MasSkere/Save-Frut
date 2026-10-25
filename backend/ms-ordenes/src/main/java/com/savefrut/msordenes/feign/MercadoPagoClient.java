package com.savefrut.msordenes.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Cliente REST de la API de Mercado Pago (Checkout Pro).
 * El token de acceso se agrega en {@link MercadoPagoFeignConfig}.
 */
@FeignClient(name = "mercado-pago", url = "${mercadopago.api-url:https://api.mercadopago.com}",
        configuration = MercadoPagoFeignConfig.class)
public interface MercadoPagoClient {

    @PostMapping("/checkout/preferences")
    Map<String, Object> crearPreferencia(@RequestHeader("X-Idempotency-Key") String idempotencyKey,
                                         @RequestBody Map<String, Object> preferencia);

    @GetMapping("/v1/payments/{id}")
    Map<String, Object> obtenerPago(@PathVariable("id") String paymentId);

    @GetMapping("/v1/payments/search")
    Map<String, Object> buscarPagos(@RequestParam("external_reference") String externalReference);
}
