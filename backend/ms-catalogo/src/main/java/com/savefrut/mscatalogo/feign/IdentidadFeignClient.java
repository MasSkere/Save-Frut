package com.savefrut.mscatalogo.feign;

import com.savefrut.mscatalogo.dto.VendedorResumenDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "ms-identidad", url = "${feign.client.ms-identidad.url:http://localhost:8081}")
public interface IdentidadFeignClient {

    @GetMapping("/internal/vendedores")
    List<VendedorResumenDTO> listarVendedores();

    @GetMapping("/internal/vendedores/usuario/{usuarioId}")
    VendedorResumenDTO obtenerPorUsuario(@PathVariable("usuarioId") Long usuarioId);
}
