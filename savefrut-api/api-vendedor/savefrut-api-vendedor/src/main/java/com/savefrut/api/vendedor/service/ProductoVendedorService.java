package com.savefrut.api.vendedor.service;

import com.savefrut.api.vendedor.dto.*;
import com.savefrut.api.common.entity.*;
import com.savefrut.api.common.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final VendedorRepository vendedorRepository;

    @Transactional(readOnly = true)
    public Optional<Vendedor> getVendedor(Long usuarioId) {
        return vendedorRepository.findByUsuarioId(usuarioId);
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
        Vendedor vendedor = vendedorRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Vendedor no encontrado"));

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
        producto.setVendedor(vendedor);
        producto.setMedicion(medicion);
        producto.setBaja(request.activo() != null ? !request.activo() : true);

        producto = productoRepository.save(producto);
        log.info("Producto {} creado por vendedor {}", producto.getId(), vendedor.getVendedorId());

        return toDTO(producto);
    }

    @Transactional
    public Optional<ProductoVendedorDTO> actualizarProducto(Long id, Long usuarioId, ProductoVendedorRequest request) {
        Vendedor vendedor = vendedorRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Vendedor no encontrado"));

        return productoRepository.findByIdAndVendedorVendedorId(id, vendedor.getVendedorId())
                .map(producto -> {
                    producto.setNombre(request.nombre());
                    producto.setDescripcion(request.descripcion());
                    producto.setPrecioNormal(request.precioNormal());
                    producto.setImagenUrl(request.imagenUrl());

                    if (request.categoriaId() != null) {
                        categoriaRepository.findById(request.categoriaId())
                                .ifPresent(producto::setCategoria);
                    }

                    producto = productoRepository.save(producto);
                    log.info("Producto {} actualizado", id);
                    return toDTO(producto);
                });
    }

    @Transactional
    public Optional<ProductoVendedorDTO> ajustarStock(Long id, Long usuarioId, Integer nuevoStock) {
        Vendedor vendedor = vendedorRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Vendedor no encontrado"));

        return productoRepository.findByIdAndVendedorVendedorId(id, vendedor.getVendedorId())
                .map(producto -> {
                    producto.setStock(nuevoStock);
                    producto = productoRepository.save(producto);
                    log.info("Stock producto {} actualizado a {}", id, nuevoStock);
                    return toDTO(producto);
                });
    }

    @Transactional
    public Optional<ProductoVendedorDTO> cambiarActivo(Long id, Long usuarioId, Boolean activo) {
        Vendedor vendedor = vendedorRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Vendedor no encontrado"));

        return productoRepository.findByIdAndVendedorVendedorId(id, vendedor.getVendedorId())
                .map(producto -> {
                    producto.setBaja(!activo);
                    producto = productoRepository.save(producto);
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
                !p.getBaja()
        );
    }
}