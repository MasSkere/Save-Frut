package com.savefrut.api.admin.service;

import com.savefrut.api.admin.dto.ProductoAdminDTO;
import com.savefrut.api.admin.dto.ProductoAdminRequest;
import com.savefrut.api.common.entity.*;
import com.savefrut.api.common.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoAdminService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final VendedorRepository vendedorRepository;
    private final TipoMedicionRepository medicionRepository;

    @Transactional(readOnly = true)
    public List<ProductoAdminDTO> getProductos(Long categoriaId, Long vendedorId, Boolean stockBajo) {
        List<Producto> productos = productoRepository.findAll().stream()
                .filter(p -> !p.getBaja())
                .filter(p -> categoriaId == null || p.getCategoria().getId().equals(categoriaId))
                .filter(p -> vendedorId == null || p.getVendedor().getVendedorId().equals(vendedorId))
                .filter(p -> stockBajo == null || (stockBajo && p.getStock() < 10))
                .toList();

        return productos.stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public ProductoAdminDTO getProductoById(Long id) {
        return productoRepository.findById(id)
                .filter(p -> !p.getBaja())
                .map(this::toDTO)
                .orElse(null);
    }

    @Transactional
    public ProductoAdminDTO crearProducto(ProductoAdminRequest request) {
        Categoria categoria = categoriaRepository.findById(request.categoriaId()).orElseThrow();
        Vendedor vendedor = vendedorRepository.findById(request.vendedorId()).orElseThrow();
        TipoMedicion medicion = medicionRepository.findById(request.medicionId()).orElseThrow();

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
        producto.setBaja(false);

        producto = productoRepository.save(producto);
        log.info("Producto creado: {}", producto.getId());
        return toDTO(producto);
    }

    @Transactional
    public ProductoAdminDTO actualizarProducto(Long id, ProductoAdminRequest request) {
        return productoRepository.findById(id)
                .filter(p -> !p.getBaja())
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
                    log.info("Producto actualizado: {}", id);
                    return toDTO(producto);
                })
                .orElse(null);
    }

    @Transactional
    public ProductoAdminDTO ajustarStock(Long id, Integer nuevoStock) {
        return productoRepository.findById(id)
                .filter(p -> !p.getBaja())
                .map(producto -> {
                    producto.setStock(nuevoStock);
                    producto = productoRepository.save(producto);
                    log.info("Stock actualizado para producto {}: {}", id, nuevoStock);
                    return toDTO(producto);
                })
                .orElse(null);
    }

    @Transactional
    public void eliminarProducto(Long id) {
        productoRepository.findById(id)
                .filter(p -> !p.getBaja())
                .ifPresent(producto -> {
                    producto.setBaja(true);
                    productoRepository.save(producto);
                    log.info("Producto dado de baja: {}", id);
                });
    }

    private ProductoAdminDTO toDTO(Producto p) {
        return new ProductoAdminDTO(
                p.getId(),
                p.getNombre(),
                p.getDescripcion(),
                p.getPrecioNormal(),
                p.getStock(),
                p.getFechaVencimiento(),
                p.getImagenUrl(),
                p.getCategoria().getNombre(),
                p.getVendedor().getRazonSocial(),
                p.getBaja()
        );
    }
}