package com.savefrut.mspuntos.feign;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-identidad", url = "${feign.client.ms-identidad.url:http://localhost:8081}")
public interface IdentidadFeignClient {

    @GetMapping("/internal/clientes/usuario/{usuarioId}")
    ClienteResumen clientePorUsuario(@PathVariable("usuarioId") Long usuarioId);

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ClienteResumen(Long id, String nombres, String apellidos) {}
}
