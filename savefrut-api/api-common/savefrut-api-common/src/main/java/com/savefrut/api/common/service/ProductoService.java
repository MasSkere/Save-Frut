package com.savefrut.api.service;

import com.savefrut.api.dto.ProductoDTO;
import com.savefrut.api.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importante

import java.time.LocalDate;
import java.util.List;

/**
 * Capa de servicio para manejar la lógica de negocio de Productos.
 * IMPORTANTE: Agrega @Transactional para solucionar el error de "InvalidDataAccessApiUsageException".
 */
@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Transactional(readOnly = true) // <-- ¡LA SOLUCIÓN AL ERROR DE LA API!
    public List<ProductoDTO> getProductosDisponibles() {
        return productoRepository.findProductosDisponibles(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> getProductosByCategoria(Long categoriaId) {
        return productoRepository.findProductosDisponiblesByCategoria(LocalDate.now(), categoriaId);
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> getProductoById(Long productoId) {
        return productoRepository.findProductoDTOById(LocalDate.now(), productoId);
    }
    
    @Transactional(readOnly = true)
    public List<ProductoDTO> getProductosByVendedor(Long vendedorId) {
        return productoRepository.findProductosDisponiblesByVendedor(LocalDate.now(), vendedorId);
    }
    
    @Transactional(readOnly = true)
    public List<ProductoDTO> buscarProductosPorNombre(String termino) {
        return productoRepository.buscarProductosPorNombre(LocalDate.now(), termino);
    }
}