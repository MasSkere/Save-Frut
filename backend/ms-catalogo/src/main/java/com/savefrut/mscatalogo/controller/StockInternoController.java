package com.savefrut.mscatalogo.controller;

import com.savefrut.mscatalogo.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints internos consumidos por ms-ordenes vía Feign (no expuestos en el API Gateway).
 */
@RestController
@RequestMapping("/internal/productos")
@RequiredArgsConstructor
public class StockInternoController {

    private final StockService stockService;

    @GetMapping("/{id}/stock")
    public Boolean verificarStock(@PathVariable Long id, @RequestParam Integer cantidad) {
        return stockService.verificarStock(id, cantidad);
    }

    @PutMapping("/{id}/stock/reservar")
    public Boolean reservarStock(@PathVariable Long id, @RequestParam Integer cantidad) {
        return stockService.reservarStock(id, cantidad);
    }

    @PutMapping("/{id}/stock/liberar")
    public Boolean liberarStock(@PathVariable Long id, @RequestParam Integer cantidad) {
        return stockService.liberarStock(id, cantidad);
    }
}
