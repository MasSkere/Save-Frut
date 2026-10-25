package com.savefrut.mspuntos.consumer;

import com.savefrut.mspuntos.config.RabbitMQConfig;
import com.savefrut.mspuntos.event.OrdenPagadaEvent;
import com.savefrut.mspuntos.service.PuntosService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrdenPagadaConsumer {

    private final PuntosService puntosService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ORDEN_PAGADA)
    public void recibirOrdenPagada(OrdenPagadaEvent evento) {
        log.info("Evento OrdenPagadaEvent recibido - ordenId: {}, clienteId: {}", evento.getOrdenId(), evento.getClienteId());
        puntosService.sumarPuntos(evento);
    }
}
