package com.savefrut.api.service;

import com.savefrut.api.dto.ProductoDTO;
import com.savefrut.api.dto.payment.PaymentIntentRequest;
import com.savefrut.api.repository.ProductoRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    // 1. Leemos la clave secreta desde application.properties
    @Value("${stripe.secret.key}")
    private String secretKey;

    @Autowired
    private ProductoRepository productoRepository; // Para buscar precios reales

    // 2. Constantes de tu lógica de negocio
    private static final BigDecimal TARIFA_VARIEDAD = new BigDecimal("5.00");

    @PostConstruct
    public void init() {
        // Configuramos la clave de Stripe globalmente
        Stripe.apiKey = secretKey;
    }

    /**
     * Lógica de negocio para crear un intento de pago.
     * Es llamada por el PaymentController.
     */
    @Transactional(readOnly = true) // Usamos @Transactional para la lectura de productos
    public PaymentIntent crearIntentoDePagoStripe(PaymentIntentRequest request) throws StripeException {

        // 1. Recalculamos el total real en el backend.
        long montoRealEnCentavos = calcularTotalReal(request);

        // 2. Creamos los parámetros para Stripe
        PaymentIntentCreateParams params =
            PaymentIntentCreateParams.builder()
                .setAmount(montoRealEnCentavos) 
                .setCurrency("pen") // Soles Peruanos
                .addPaymentMethodType("card")
                .build();

        // 3. Llamamos a la API de Stripe y devolvemos el objeto
        return PaymentIntent.create(params);
    }

    /**
     * ¡LÓGICA DE NEGOCIO "CONFIANZA CERO"!
     * Recalcula el precio total basándose en los datos de la BD.
     */
    private long calcularTotalReal(PaymentIntentRequest request) {
        
        Set<Long> productoIds = request.getItems().stream()
                .map(PaymentIntentRequest.Item::getId)
                .collect(Collectors.toSet());
        
        // 1. Traemos los productos REALES de la BD (usando nuestro SP)
        Map<Long, ProductoDTO> productosReales = productoRepository.findAllById(productoIds).stream()
            .map(p -> productoRepository.findProductoDTOById(LocalDate.now(), p.getId()).stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + p.getId())))
            .collect(Collectors.toMap(ProductoDTO::getId, dto -> dto));


        BigDecimal subtotalReal = BigDecimal.ZERO;
        
        // 2. Recalculamos Subtotal
        for (PaymentIntentRequest.Item itemCliente : request.getItems()) {
            ProductoDTO productoReal = productosReales.get(itemCliente.getId());
            
            // ¡Usamos el precio con descuento DE LA BD!
            subtotalReal = subtotalReal.add(
                productoReal.getPrecioDescuento().multiply(BigDecimal.valueOf(itemCliente.getCantidad()))
            );
        }
        
        // 3. Calcular Tarifa por Variedad
        Set<Long> vendedorIds = productosReales.values().stream()
                .map(ProductoDTO::getVendedorId)
                .collect(Collectors.toSet());
        
        BigDecimal tarifaVariedadReal = (vendedorIds.size() > 1) ? TARIFA_VARIEDAD : BigDecimal.ZERO;

        // 4. Validar Costo de Envío
        BigDecimal costoEnvioReal = BigDecimal.valueOf(request.getCostoEnvio());
        
        // 5. Calcular Total REAL
        BigDecimal totalReal = subtotalReal.add(tarifaVariedadReal).add(costoEnvioReal);

        // 6. Convertir a centavos para Stripe (ej. 17.20 -> 1720)
        return totalReal.multiply(new BigDecimal("100")).longValue();
    }
}