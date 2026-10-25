package com.savefrut.mscatalogo.service;

import com.savefrut.mscatalogo.dto.VendedorResumenDTO;
import com.savefrut.mscatalogo.dto.vendedor.ProductoVendedorDTO;
import com.savefrut.mscatalogo.dto.vendedor.ProductoVendedorRequest;
import com.savefrut.mscatalogo.entity.Categoria;
import com.savefrut.mscatalogo.entity.Producto;
import com.savefrut.mscatalogo.entity.TipoMedicion;
import com.savefrut.mscatalogo.repository.CategoriaRepository;
import com.savefrut.mscatalogo.repository.ProductoRepository;
import com.savefrut.mscatalogo.repository.TipoMedicionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoVendedorService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final TipoMedicionRepository medicionRepository;
    private final VendedorConsultaService vendedorConsultaService;
    private final CatalogoCacheJob catalogoCacheJob;

    @Transactional(readOnly = true)
    public Optional<VendedorResumenDTO> getVendedor(Long usuarioId) {
        return vendedorConsultaService.getVendedorPorUsuario(usuarioId);
    }

    @Transactional(readOnly = true)
    public List<ProductoVendedorDTO> getProductos(Long vendedorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Producto> productos = productoRepository.findByVendedorVendedorId(vendedorId, pageable);
        return productos.getContent().stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ProductoVendedorDTO> getProductoById(Long id, Long vendedorId) {
        return productoRepository.findByIdAndVendedorVendedorId(id, vendedorId)
                .map(this::toDTO);
    }

    @Transactional
    public ProductoVendedorDTO crearProducto(Long usuarioId, ProductoVendedorRequest request) {
        VendedorResumenDTO vendedor = vendedorConsultaService.getVendedorPorUsuario(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Vendedor no encontrado"));
        if (!Boolean.TRUE.equals(vendedor.aprobado())) {
            throw new IllegalStateException("Tu tienda aún no ha sido aprobada por un administrador");
        }

        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        TipoMedicion medicion = medicionRepository.findById(request.medicionId())
                .orElseThrow(() -> new IllegalArgumentException("Medición no encontrada"));

        Producto producto = new Producto();
        producto.setNombre(request.nombre());
        producto.setDescripcion(request.descripcion());
        producto.setPrecioNormal(request.precioNormal());
        producto.setStock(request.stock());
        producto.setFechaVencimiento(request.fechaVencimiento());
        producto.setImagenUrl(request.imagenUrl());
        producto.setCategoria(categoria);
        producto.setVendedorId(vendedor.id());
        producto.setMedicion(medicion);
        producto.setBaja(false);

        producto = productoRepository.save(producto);
        catalogoCacheJob.invalidarCatalogo();
        log.info("Producto {} creado por vendedor {}", producto.getId(), vendedor.id());

        return toDTO(producto);
    }

    @Transactional
    public Optional<ProductoVendedorDTO> actualizarProducto(Long id, Long usuarioId, ProductoVendedorRequest request) {
        VendedorResumenDTO vendedor = vendedorConsultaService.getVendedorPorUsuario(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Vendedor no encontrado"));

        return productoRepository.findByIdAndVendedorVendedorId(id, vendedor.id())
                .map(producto -> {
                    producto.setNombre(request.nombre());
                    producto.setDescripcion(request.descripcion());
                    producto.setPrecioNormal(request.precioNormal());
                    producto.setImagenUrl(request.imagenUrl());
                    if (request.stock() != null) {
                        producto.setStock(request.stock());
                    }
                    if (request.fechaVencimiento() != null) {
                        producto.setFechaVencimiento(request.fechaVencimiento());
                    }
                    if (request.categoriaId() != null) {
                        categoriaRepository.findById(request.categoriaId())
                                .ifPresent(producto::setCategoria);
                    }
                    if (request.medicionId() != null) {
                        medicionRepository.findById(request.medicionId())
                                .ifPresent(producto::setMedicion);
                    }

                    producto = productoRepository.save(producto);
                    catalogoCacheJob.invalidarCatalogo();
                    log.info("Producto {} actualizado", id);
                    return toDTO(producto);
                });
    }

    @Transactional
    public Optional<ProductoVendedorDTO> ajustarStock(Long id, Long usuarioId, Integer nuevoStock) {
        VendedorResumenDTO vendedor = vendedorConsultaService.getVendedorPorUsuario(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Vendedor no encontrado"));

        return productoRepository.findByIdAndVendedorVendedorId(id, vendedor.id())
                .map(producto -> {
                    try {
                        producto.setStock(nuevoStock);
                        producto = productoRepository.save(producto);
                        catalogoCacheJob.invalidarCatalogo();
                        log.info("Stock producto {} actualizado a {}", id, nuevoStock);
                        return toDTO(producto);
                    } catch (ObjectOptimisticLockingFailureException e) {
                        log.error("Concurrency conflict while adjusting stock for product {}", id);
                        throw new IllegalStateException("Conflicto de concurrencia al ajustar el stock. Intente nuevamente.");
                    }
                });
    }

    @Transactional
    public Optional<ProductoVendedorDTO> cambiarActivo(Long id, Long usuarioId, Boolean activo) {
        VendedorResumenDTO vendedor = vendedorConsultaService.getVendedorPorUsuario(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Vendedor no encontrado"));

        return productoRepository.findByIdAndVendedorVendedorId(id, vendedor.id())
                .map(producto -> {
                    producto.setBaja(!activo);
                    producto = productoRepository.save(producto);
                    catalogoCacheJob.invalidarCatalogo();
                    log.info("Producto {} estado: {}", id, activo ? "activo" : "pausado");
                    return toDTO(producto);
                });
    }

    private ProductoVendedorDTO toDTO(Producto p) {
        int dias = LocalDate.now().until(p.getFechaVencimiento()).getDays();
        BigDecimal descuento = dias <= 2
                ? p.getPrecioNormal().multiply(new BigDecimal("0.40"))
                : p.getPrecioNormal().multiply(new BigDecimal("0.70"));

        return new ProductoVendedorDTO(
                p.getId(),
                p.getNombre(),
                p.getDescripcion(),
                p.getPrecioNormal(),
                descuento.setScale(2, RoundingMode.HALF_UP),
                p.getStock(),
                p.getFechaVencimiento(),
                p.getImagenUrl(),
                p.getCategoria().getNombre(),
                !p.getBaja(),
                p.getCategoria().getId(),
                p.getMedicion().getId()
        );
    }
}
