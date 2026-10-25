package com.savefrut.msordenes.feign;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

/**
 * Configuración exclusiva del cliente de Mercado Pago (sin @Configuration para no aplicarse a otros clientes Feign).
 */
public class MercadoPagoFeignConfig {

    @Bean
    public RequestInterceptor mercadoPagoAuthInterceptor(@Value("${mercadopago.access-token:}") String accessToken) {
        return template -> template.header("Authorization", "Bearer " + accessToken);
    }
}
