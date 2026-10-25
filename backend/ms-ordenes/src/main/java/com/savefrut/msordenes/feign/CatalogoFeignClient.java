package com.savefrut.msordenes.feign;

import com.savefrut.msordenes.dto.ProductoCatalogoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ms-catalogo", url = "${feign.client.ms-catalogo.url:http://localhost:8082}")
public interface CatalogoFeignClient {

    @GetMapping("/productos/{id}")
    ProductoCatalogoDTO obtenerProducto(@PathVariable("id") Long productoId);

    @GetMapping("/internal/productos/{id}/stock")
    Boolean verificarStock(@PathVariable("id") Long productoId, @RequestParam("cantidad") Integer cantidad);

    @PutMapping("/internal/productos/{id}/stock/reservar")
    Boolean reservarStock(@PathVariable("id") Long productoId, @RequestParam("cantidad") Integer cantidad);

    @PutMapping("/internal/productos/{id}/stock/liberar")
    Boolean liberarStock(@PathVariable("id") Long productoId, @RequestParam("cantidad") Integer cantidad);
}
