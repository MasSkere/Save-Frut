package com.savefrut.api.common.service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import com.savefrut.api.common.dto.payment.PaymentIntentRequest;
import com.savefrut.api.common.entity.Producto;
import com.savefrut.api.common.repository.ProductoRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final BigDecimal TARIFA_VARIEDAD_SOLES = new BigDecimal("5.00");
    private static final String CURRENCY_PEN = "PEN";
    private static final String DESCRIPCION_COMPRA = "Compra en SaveFrut";
    private static final int DIAS_DESCUENTO_MAXIMO = 7;
    private static final int DIAS_DESCUENTO_CERCANO = 2;
    private static final BigDecimal DESCUENTO_LEJANO = new BigDecimal("0.70");
    private static final BigDecimal DESCUENTO_CERCANO = new BigDecimal("0.40");

    @Value("${mercadopago.access-token}")
    private String accessToken;

    private final ProductoRepository productoRepository;

    @PostConstruct
    public void init() {
        MercadoPagoConfig.setAccessToken(accessToken);
        log.info("MercadoPago SDK inicializado correctamente.");
    }

    @Transactional(readOnly = true)
    public Preference crearPreferenciaDePago(PaymentIntentRequest request)
            throws MPException, MPApiException {

        BigDecimal totalEnSoles = calcularTotalVerificado(request);
        log.debug("Total verificado para preferencia MP: S/ {}", totalEnSoles);

        PreferenceRequest preferenceRequest = buildPreferenceRequest(totalEnSoles);
        return new PreferenceClient().create(preferenceRequest);
    }

    private PreferenceRequest buildPreferenceRequest(BigDecimal totalEnSoles) {
        PreferenceItemRequest item = PreferenceItemRequest.builder()
                .title(DESCRIPCION_COMPRA)
                .quantity(1)
                .unitPrice(totalEnSoles)
                .currencyId(CURRENCY_PEN)
                .build();

        return PreferenceRequest.builder()
                .items(List.of(item))
                .build();
    }

    private BigDecimal calcularTotalVerificado(PaymentIntentRequest request) {
        Map<Long, ItemVerificado> productosVerificados = obtenerProductosVerificados(request);

        BigDecimal subtotal = calcularSubtotal(request.getItems(), productosVerificados);
        BigDecimal tarifaVariedad = calcularTarifaVariedad(productosVerificados);
        BigDecimal costoEnvio = BigDecimal.valueOf(request.getCostoEnvio());

        return subtotal.add(tarifaVariedad).add(costoEnvio);
    }

    private Map<Long, ItemVerificado> obtenerProductosVerificados(PaymentIntentRequest request) {
        Set<Long> idsRequeridos = extraerIdsDeItems(request.getItems());
        LocalDate hoy = LocalDate.now();

        return idsRequeridos.stream()
                .map(id -> productoRepository
                        .findByIdAvailable(id, hoy, hoy.plusDays(DIAS_DESCUENTO_MAXIMO))
                        .stream()
                        .findFirst()
                        .map(this::toItemVerificado)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Producto no encontrado o sin precio vigente: " + id)))
                .collect(Collectors.toMap(item -> item.id, item -> item));
    }

    private ItemVerificado toItemVerificado(Producto p) {
        int diasParaVencer = LocalDate.now().until(p.getFechaVencimiento()).getDays();
        BigDecimal precioDescuento = diasParaVencer <= DIAS_DESCUENTO_CERCANO
                ? p.getPrecioNormal().multiply(DESCUENTO_CERCANO)
                : p.getPrecioNormal().multiply(DESCUENTO_LEJANO);
        precioDescuento = precioDescuento.setScale(2, RoundingMode.HALF_UP);

        return new ItemVerificado(p.getId(), precioDescuento, p.getVendedor().getVendedorId());
    }

    private Set<Long> extraerIdsDeItems(List<PaymentIntentRequest.Item> items) {
        return items.stream()
                .map(PaymentIntentRequest.Item::getId)
                .collect(Collectors.toSet());
    }

    private BigDecimal calcularSubtotal(List<PaymentIntentRequest.Item> items,
                                    Map<Long, ItemVerificado> productosPorId) {
        return items.stream()
                .map(item -> {
                    ItemVerificado producto = productosPorId.get(item.getId());
                    return producto.precioDescuento.multiply(BigDecimal.valueOf(item.getCantidad()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcularTarifaVariedad(Map<Long, ItemVerificado> productosPorId) {
        long cantidadVendedores = productosPorId.values().stream()
                .map(ItemVerificado::vendedorId)
                .distinct()
                .count();

        return cantidadVendedores > 1 ? TARIFA_VARIEDAD_SOLES : BigDecimal.ZERO;
    }

    // Así de limpia queda tu clase contenedora de datos
    private record ItemVerificado(Long id, BigDecimal precioDescuento, Long vendedorId) {}
}