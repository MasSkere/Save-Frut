package com.savefrut.msordenes.feign;

import com.savefrut.msordenes.dto.ClienteResumenDTO;
import com.savefrut.msordenes.dto.EstadisticasIdentidadDTO;
import com.savefrut.msordenes.dto.VendedorResumenDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "ms-identidad", url = "${feign.client.ms-identidad.url:http://localhost:8081}")
public interface IdentidadFeignClient {

    @GetMapping("/internal/clientes/usuario/{usuarioId}")
    ClienteResumenDTO clientePorUsuario(@PathVariable("usuarioId") Long usuarioId);

    @GetMapping("/internal/clientes")
    List<ClienteResumenDTO> listarClientes();

    @GetMapping("/internal/vendedores/usuario/{usuarioId}")
    VendedorResumenDTO vendedorPorUsuario(@PathVariable("usuarioId") Long usuarioId);

    @GetMapping("/internal/estadisticas")
    EstadisticasIdentidadDTO estadisticas();
}
