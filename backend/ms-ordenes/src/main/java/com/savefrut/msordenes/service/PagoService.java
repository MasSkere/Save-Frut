package com.savefrut.msordenes.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.savefrut.msordenes.config.RabbitMQConfig;
import com.savefrut.msordenes.dto.ClienteResumenDTO;
import com.savefrut.msordenes.dto.OrdenDTO;
import com.savefrut.msordenes.dto.PreferenciaPagoDTO;
import com.savefrut.msordenes.entity.DetalleOrden;
import com.savefrut.msordenes.entity.EstadoOrden;
import com.savefrut.msordenes.entity.Orden;
import com.savefrut.msordenes.event.OrdenPagadaEvent;
import com.savefrut.msordenes.idempotency.IdempotencyService;
import com.savefrut.msordenes.repository.DetalleOrdenRepository;
import com.savefrut.msordenes.repository.OrdenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Flujo de pago con Mercado Pago Checkout Pro:
 * 1) crear preferencia (redirige al usuario a MP), 2) confirmar el pago al volver (o vía webhook),
 * 3) publicar OrdenPagadaEvent en RabbitMQ para que ms-puntos sume Eco-Puntos.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PagoService {

    private final OrdenRepository ordenRepository;
    private final DetalleOrdenRepository detalleOrdenRepository;
    private final OrdenService ordenService;
    private final ConsultaUsuariosService consultaUsuariosService;
    private final MercadoPagoGateway mercadoPagoGateway;
    private final IdempotencyService idempotencyService;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${mercadopago.access-token:}")
    private String accessToken;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Value("${mercadopago.webhook-url:}")
    private String webhookUrl;

    public PreferenciaPagoDTO crearPreferencia(Long ordenId, Long usuarioId, String idempotencyKey) {
        if (!StringUtils.hasText(idempotencyKey)) {
            throw new IllegalArgumentException("El header Idempotency-Key es obligatorio");
        }
        var previa = idempotencyService.obtenerRespuesta(idempotencyKey);
        if (previa.isPresent()) {
            log.info("Idempotency-Key repetida para orden {}, se devuelve la preferencia ya creada", ordenId);
            return leer(previa.get());
        }

        ClienteResumenDTO cliente = consultaUsuariosService.clienteDe(usuarioId);
        Orden orden = ordenService.obtenerOrden(ordenId);
        validarOrdenPagable(orden, cliente);
        if (!StringUtils.hasText(accessToken)) {
            throw new IllegalStateException("Mercado Pago no está configurado (MERCADO_PAGO_ACCESS_TOKEN vacío)");
        }

        Map<String, Object> respuesta = mercadoPagoGateway.crearPreferencia(
                idempotencyKey, construirPreferencia(orden, cliente, detalleOrdenRepository.findByOrdenId(ordenId)));

        // Con credenciales de prueba (TEST-...) el pago debe hacerse en el checkout sandbox
        String sandboxUrl = (String) respuesta.get("sandbox_init_point");
        String urlPago = accessToken.startsWith("TEST-") && sandboxUrl != null
                ? sandboxUrl
                : (String) respuesta.get("init_point");
        PreferenciaPagoDTO preferencia = new PreferenciaPagoDTO(
                ordenId,
                String.valueOf(respuesta.get("id")),
                urlPago,
                sandboxUrl);

        guardarPreferenceId(ordenId, preferencia.preferenceId());
        idempotencyService.guardarRespuesta(idempotencyKey, escribir(preferencia));
        log.info("Preferencia {} creada para la orden {}", preferencia.preferenceId(), ordenId);
        return preferencia;
    }

    public OrdenDTO confirmarPago(Long ordenId, String paymentId, Long usuarioId) {
        ClienteResumenDTO cliente = consultaUsuariosService.clienteDe(usuarioId);
        Orden orden = ordenService.obtenerOrden(ordenId);
        if (!orden.getClienteId().equals(cliente.id())) {
            throw new SecurityException("Acceso denegado: esta orden no le pertenece.");
        }

        Map<String, Object> pago = mercadoPagoGateway.obtenerPago(paymentId);
        String referencia = String.valueOf(pago.get("external_reference"));
        String estadoPago = String.valueOf(pago.get("status"));

        if (!referenciaExterna(orden).equals(referencia)) {
            throw new IllegalArgumentException("El pago no corresponde a esta orden");
        }
        if ("approved".equals(estadoPago)) {
            marcarPagada(ordenId, paymentId);
        } else {
            log.info("Pago {} de la orden {} en estado {}", paymentId, ordenId, estadoPago);
        }
        Orden actualizada = ordenService.obtenerOrden(ordenId);
        return OrdenService.toDTO(actualizada, detalleOrdenRepository.findByOrdenId(ordenId), cliente.nombreCompleto());
    }

    /** Verificación manual del cliente ("Ya pagué"): concilia la orden y devuelve su estado actual. */
    public OrdenDTO verificarPago(Long ordenId, Long usuarioId) {
        ClienteResumenDTO cliente = consultaUsuariosService.clienteDe(usuarioId);
        Orden orden = ordenService.obtenerOrden(ordenId);
        if (!orden.getClienteId().equals(cliente.id())) {
            throw new SecurityException("Acceso denegado: esta orden no le pertenece.");
        }
        conciliar(ordenId);
        return OrdenService.toDTO(ordenService.obtenerOrden(ordenId), detalleOrdenRepository.findByOrdenId(ordenId),
                cliente.nombreCompleto());
    }

    /**
     * Busca en Mercado Pago los pagos de la orden (external_reference) y la marca como pagada si hay uno aprobado.
     * Reemplaza al webhook cuando el backend no tiene URL pública (entorno local).
     */
    @SuppressWarnings("unchecked")
    public boolean conciliar(Long ordenId) {
        Orden orden = ordenService.obtenerOrden(ordenId);
        Map<String, Object> respuesta = mercadoPagoGateway.buscarPagos(referenciaExterna(orden));
        List<Map<String, Object>> pagos = (List<Map<String, Object>>) respuesta.getOrDefault("results", List.of());
        return pagos.stream()
                .filter(p -> "approved".equals(String.valueOf(p.get("status"))))
                .filter(p -> montoCoincide(p, orden))
                .findFirst()
                .map(p -> {
                    marcarPagada(ordenId, String.valueOf(p.get("id")));
                    return true;
                })
                .orElse(false);
    }

    /** Notificación asíncrona de Mercado Pago (topic payment). */
    public void procesarWebhook(String paymentId) {
        Map<String, Object> pago = mercadoPagoGateway.obtenerPago(paymentId);
        if (!"approved".equals(String.valueOf(pago.get("status")))) {
            return;
        }
        try {
            String referencia = String.valueOf(pago.get("external_reference"));
            Long ordenId = ordenIdDe(referencia);
            if (referenciaExterna(ordenService.obtenerOrden(ordenId)).equals(referencia)) {
                marcarPagada(ordenId, paymentId);
            }
        } catch (RuntimeException e) {
            log.warn("Webhook con external_reference inválido para el pago {}", paymentId);
        }
    }

    /**
     * Idempotente: si la orden ya fue pagada no vuelve a publicar el evento (evita puntos duplicados).
     */
    @Transactional
    public void marcarPagada(Long ordenId, String paymentId) {
        if (ordenRepository.marcarPagada(ordenId, paymentId, LocalDateTime.now()) == 0) {
            log.info("Orden {} ya procesada o inexistente, se ignora el pago {}", ordenId, paymentId);
            return;
        }
        Orden orden = ordenService.obtenerOrden(ordenId);

        int unidades = detalleOrdenRepository.findByOrdenId(ordenId).stream().mapToInt(DetalleOrden::getCantidad).sum();
        OrdenPagadaEvent evento = new OrdenPagadaEvent(ordenId, orden.getClienteId(), orden.getTotal(), orden.getFechaPago(), unidades);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_ORDENES, RabbitMQConfig.ROUTING_KEY_ORDEN_PAGADA, evento);
        log.info("Orden {} pagada (pago {}). OrdenPagadaEvent publicado", ordenId, paymentId);
    }

    /**
     * Referencia única por orden (SF-{id}-{fecha}). Con solo el id, al reiniciar la BD los ids se repiten
     * y la conciliación encontraba pagos aprobados de órdenes antiguas en la misma cuenta de Mercado Pago.
     */
    static String referenciaExterna(Orden orden) {
        return "SF-" + orden.getId() + "-" + orden.getFechaOrden().toEpochSecond(ZoneOffset.UTC);
    }

    private static Long ordenIdDe(String referencia) {
        return Long.valueOf(referencia.split("-")[1]);
    }

    private static boolean montoCoincide(Map<String, Object> pago, Orden orden) {
        Object monto = pago.get("transaction_amount");
        return monto != null && new BigDecimal(String.valueOf(monto)).compareTo(orden.getTotal()) == 0;
    }

    private void validarOrdenPagable(Orden orden, ClienteResumenDTO cliente) {
        if (!orden.getClienteId().equals(cliente.id())) {
            throw new SecurityException("Acceso denegado: esta orden no le pertenece.");
        }
        if (!EstadoOrden.PENDIENTE_PAGO.equals(orden.getEstadoOrden())) {
            throw new IllegalStateException("La orden ya fue pagada o cancelada (estado " + orden.getEstadoOrden() + ")");
        }
    }

    private Map<String, Object> construirPreferencia(Orden orden, ClienteResumenDTO cliente, List<DetalleOrden> detalles) {
        List<Map<String, Object>> items = new java.util.ArrayList<>(detalles.stream()
                .map(d -> item(d.getNombreProducto(), d.getCantidad(), d.getPrecioUnitario().doubleValue()))
                .toList());
        if (orden.getCostoEnvio().signum() > 0) {
            items.add(item("Costo de envío", 1, orden.getCostoEnvio().doubleValue()));
        }
        if (orden.getTarifaVariedad().signum() > 0) {
            items.add(item("Tarifa por variedad de tiendas", 1, orden.getTarifaVariedad().doubleValue()));
        }

        String retorno = frontendUrl + "/checkout/resultado?ordenId=" + orden.getId();
        Map<String, Object> preferencia = new HashMap<>();
        preferencia.put("items", items);
        preferencia.put("external_reference", referenciaExterna(orden));
        // En sandbox el comprador debe ser un usuario de prueba de MP; enviar el email real provoca rechazos
        if (!accessToken.startsWith("TEST-") && cliente.email() != null) {
            preferencia.put("payer", Map.of("email", cliente.email()));
        }
        preferencia.put("back_urls", Map.of("success", retorno, "failure", retorno, "pending", retorno));
        // MP solo acepta auto_return con URLs https públicas; en localhost el frontend espera el pago por polling
        if (frontendUrl.startsWith("https://")) {
            preferencia.put("auto_return", "approved");
        }
        preferencia.put("statement_descriptor", "SAVEFRUT");
        if (StringUtils.hasText(webhookUrl)) {
            preferencia.put("notification_url", webhookUrl);
        }
        return preferencia;
    }

    private Map<String, Object> item(String titulo, int cantidad, double precio) {
        return Map.of(
                "title", titulo,
                "quantity", cantidad,
                "unit_price", precio,
                "currency_id", "PEN");
    }

    private void guardarPreferenceId(Long ordenId, String preferenceId) {
        Orden orden = ordenService.obtenerOrden(ordenId);
        orden.setMpPreferenceId(preferenceId);
        ordenRepository.save(orden);
    }

    private String escribir(PreferenciaPagoDTO preferencia) {
        try {
            return objectMapper.writeValueAsString(preferencia);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar la preferencia", e);
        }
    }

    private PreferenciaPagoDTO leer(String json) {
        try {
            return objectMapper.readValue(json, PreferenciaPagoDTO.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Respuesta idempotente corrupta", e);
        }
    }
}
