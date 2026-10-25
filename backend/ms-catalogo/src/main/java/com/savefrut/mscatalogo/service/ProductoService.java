package com.savefrut.mscatalogo.service;

import com.savefrut.mscatalogo.dto.ProductoDTO;
import com.savefrut.mscatalogo.entity.Producto;
import com.savefrut.mscatalogo.repository.ProductoRepository;
import com.savefrut.mscatalogo.common.util.ImageUrlUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ImageUrlUtil imageUrlUtil;
    private final CatalogoCacheJob catalogoCacheJob;
    private final VendedorConsultaService vendedorConsultaService;

    private static final int DIAS_DESCUENTO_MAXIMO = 7;
    private static final int DIAS_DESCUENTO_MAXIMO_CERCANO = 2;
    private static final BigDecimal DESCUENTO_LEJANO = new BigDecimal("0.70");
    private static final BigDecimal DESCUENTO_CERCANO = new BigDecimal("0.40");

    public List<ProductoDTO> getProductosDisponibles() {
        return catalogoCacheJob.obtenerCatalogoRedis();
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> getProductosByCategoria(Long categoriaId) {
        LocalDate hoy = LocalDate.now();
        List<Producto> productos = productoRepository.findByCategoria(hoy, hoy.plusDays(DIAS_DESCUENTO_MAXIMO), categoriaId);
        return publicos(productos);
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> getProductoById(Long productoId) {
        LocalDate hoy = LocalDate.now();
        List<Producto> productos = productoRepository.findByIdAvailable(productoId, hoy, hoy.plusDays(DIAS_DESCUENTO_MAXIMO));
        return publicos(productos);
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> getProductosByVendedor(Long vendedorId) {
        LocalDate hoy = LocalDate.now();
        List<Producto> productos = productoRepository.findByVendedor(hoy, hoy.plusDays(DIAS_DESCUENTO_MAXIMO), vendedorId);
        return publicos(productos);
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> buscarProductosPorNombre(String termino) {
        LocalDate hoy = LocalDate.now();
        List<Producto> productos = productoRepository.findByNombreContaining(hoy, hoy.plusDays(DIAS_DESCUENTO_MAXIMO), termino);
        return publicos(productos).stream().limit(5).toList();
    }

    /** Solo se exponen productos de tiendas aprobadas. */
    private List<ProductoDTO> publicos(List<Producto> productos) {
        Map<Long, String> aprobados = vendedorConsultaService.getVendedoresAprobados();
        return productos.stream()
                .filter(p -> aprobados == null || aprobados.containsKey(p.getVendedorId()))
                .map(p -> toDTO(p, aprobados == null ? Map.of() : aprobados))
                .toList();
    }

    private ProductoDTO toDTO(Producto producto, Map<Long, String> nombresVendedores) {
        int diasParaVencer = LocalDate.now().until(producto.getFechaVencimiento()).getDays();
        BigDecimal precioDescuento = calcularDescuento(producto.getPrecioNormal(), diasParaVencer);

        ProductoDTO dto = new ProductoDTO(
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
        return dto;
    }

    private BigDecimal calcularDescuento(BigDecimal precioNormal, int diasParaVencer) {
        BigDecimal porcentaje = diasParaVencer <= DIAS_DESCUENTO_MAXIMO_CERCANO ? DESCUENTO_CERCANO : DESCUENTO_LEJANO;
        return precioNormal.multiply(porcentaje).setScale(2, RoundingMode.HALF_UP);
    }
}
