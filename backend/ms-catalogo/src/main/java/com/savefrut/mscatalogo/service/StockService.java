package com.savefrut.mscatalogo.service;

import com.savefrut.mscatalogo.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final ProductoRepository productoRepository;
    private final CatalogoCacheJob catalogoCacheJob;

    @Transactional(readOnly = true)
    public Boolean verificarStock(Long productoId, Integer cantidad) {
        return productoRepository.findById(productoId)
                .map(p -> !Boolean.TRUE.equals(p.getBaja()) && p.getStock() >= cantidad)
                .orElse(false);
    }

    @Transactional
    public Boolean reservarStock(Long productoId, Integer cantidad) {
        try {
            return productoRepository.findById(productoId)
                    .filter(p -> p.getStock() >= cantidad)
                    .map(p -> {
                        p.setStock(p.getStock() - cantidad);
                        productoRepository.save(p);
                        catalogoCacheJob.invalidarCatalogo();
                        log.info("Stock reservado producto {}: -{}", productoId, cantidad);
                        return true;
                    })
                    .orElse(false);
        } catch (ObjectOptimisticLockingFailureException e) {
            log.error("Conflicto de concurrencia al reservar stock del producto {}", productoId);
            return false;
        }
    }

    @Transactional
    public Boolean liberarStock(Long productoId, Integer cantidad) {
        return productoRepository.findById(productoId)
                .map(p -> {
                    p.setStock(p.getStock() + cantidad);
                    productoRepository.save(p);
                    catalogoCacheJob.invalidarCatalogo();
                    log.info("Stock liberado producto {}: +{}", productoId, cantidad);
                    return true;
                })
                .orElse(false);
    }
}
