package com.savefrut.mspuntos.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableRabbit
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_ORDENES = "ordenes.exchange";
    public static final String QUEUE_ORDEN_PAGADA = "orden.pagada.queue";
    public static final String ROUTING_KEY_ORDEN_PAGADA = "orden.pagada";

    @Bean
    public DirectExchange ordenesExchange() {
        return new DirectExchange(EXCHANGE_ORDENES);
    }

    @Bean
    public Queue ordenPagadaQueue() {
        return new Queue(QUEUE_ORDEN_PAGADA, true);
    }

    @Bean
    public Binding ordenPagadaBinding(Queue ordenPagadaQueue, DirectExchange ordenesExchange) {
        return BindingBuilder.bind(ordenPagadaQueue).to(ordenesExchange).with(ROUTING_KEY_ORDEN_PAGADA);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
