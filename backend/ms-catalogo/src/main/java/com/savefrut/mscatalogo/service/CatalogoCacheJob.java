package com.savefrut.mscatalogo.service;

import com.savefrut.mscatalogo.dto.ProductoDTO;
import com.savefrut.mscatalogo.entity.Producto;
import com.savefrut.mscatalogo.repository.ProductoRepository;
import com.savefrut.mscatalogo.common.util.ImageUrlUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class CatalogoCacheJob {

    private static final String CATALOGO_CACHE_KEY = "CATALOGO_PUBLICO";
    private static final int CATALOGO_CACHE_TTL_MINUTES = 10;
    private static final int DIAS_DESCUENTO_MAXIMO = 7;
    private static final int DIAS_DESCUENTO_MAXIMO_CERCANO = 2;
    private static final BigDecimal DESCUENTO_LEJANO = new BigDecimal("0.70");
    private static final BigDecimal DESCUENTO_CERCANO = new BigDecimal("0.40");

    private final ProductoRepository productoRepository;
    private final ImageUrlUtil imageUrlUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final VendedorConsultaService vendedorConsultaService;

    @Scheduled(fixedRate = 60000)
    @Transactional(readOnly = true)
    public void refrescarCatalogoProgramado() {
        refrescarCatalogoRedis();
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> refrescarCatalogoRedis() {
        log.info("Iniciando refresco del catálogo en Redis...");
        long startTime = System.currentTimeMillis();
        List<ProductoDTO> catalogosDTO = Collections.emptyList();

        try {
            LocalDate hoy = LocalDate.now();
            LocalDate hoyPlus7 = hoy.plusDays(DIAS_DESCUENTO_MAXIMO);

            List<Producto> productos = productoRepository.findDisponibles(hoy, hoyPlus7);

            Map<Long, String> aprobados = vendedorConsultaService.getVendedoresAprobados();
            catalogosDTO = productos.stream()
                    .filter(p -> aprobados == null || aprobados.containsKey(p.getVendedorId()))
                    .map(p -> toDTO(p, aprobados == null ? Map.of() : aprobados))
                    .collect(Collectors.toCollection(ArrayList::new));

            redisTemplate.opsForValue().set(CATALOGO_CACHE_KEY, catalogosDTO, CATALOGO_CACHE_TTL_MINUTES, TimeUnit.MINUTES);

            long duration = System.currentTimeMillis() - startTime;
            log.info("Catálogo refrescado en Redis: {} productos en {} ms", catalogosDTO.size(), duration);

        } catch (Exception e) {
            log.error("Error al refrescar catálogo en Redis: {}", e.getMessage(), e);
        }
        return catalogosDTO;
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> obtenerCatalogoRedis() {
        try {
            Object cached = redisTemplate.opsForValue().get(CATALOGO_CACHE_KEY);
            if (cached instanceof List<?> list) {
                @SuppressWarnings("unchecked")
                List<ProductoDTO> productos = (List<ProductoDTO>) list;
                log.debug("Catálogo obtenido de Redis: {} productos", productos.size());
                return productos;
            }
        } catch (Exception e) {
            log.warn("Error al obtener catálogo de Redis: {}", e.getMessage());
        }
        log.info("Catálogo no encontrado en Redis (cache miss), consultando base de datos");
        return refrescarCatalogoRedis();
    }

    public void invalidarCatalogo() {
        try {
            redisTemplate.delete(CATALOGO_CACHE_KEY);
            log.info("Catálogo en Redis invalidado");
        } catch (Exception e) {
            log.error("Error al invalidar catálogo en Redis: {}", e.getMessage());
        }
    }

    private ProductoDTO toDTO(Producto producto, Map<Long, String> nombresVendedores) {
        int diasParaVencer = LocalDate.now().until(producto.getFechaVencimiento()).getDays();
        BigDecimal precioDescuento = calcularDescuento(producto.getPrecioNormal(), diasParaVencer);

        return new ProductoDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecioNormal(),
                precioDescuento,
                producto.getFechaVencimiento(),
                diasParaVencer,
                imageUrlUtil.getValidImageUrl(producto.getImagenUrl()),
                producto.getMedicion().getNombre(),
                producto.getCategoria().getNombre(),
                nombresVendedores.get(producto.getVendedorId()),
                producto.getVendedorId()
        );
    }

    private BigDecimal calcularDescuento(BigDecimal precioNormal, int diasParaVencer) {
        BigDecimal porcentaje = diasParaVencer <= DIAS_DESCUENTO_MAXIMO_CERCANO ? DESCUENTO_CERCANO : DESCUENTO_LEJANO;
        return precioNormal.multiply(porcentaje).setScale(2, RoundingMode.HALF_UP);
    }
}
