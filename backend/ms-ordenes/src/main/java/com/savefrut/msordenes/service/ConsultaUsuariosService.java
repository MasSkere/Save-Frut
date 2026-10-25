package com.savefrut.msordenes.service;

import com.savefrut.msordenes.dto.ClienteResumenDTO;
import com.savefrut.msordenes.dto.EstadisticasIdentidadDTO;
import com.savefrut.msordenes.feign.IdentidadFeignClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Consultas síncronas (OpenFeign) a ms-identidad.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultaUsuariosService {

    private final IdentidadFeignClient identidadFeignClient;

    public ClienteResumenDTO clienteDe(Long usuarioId) {
        try {
            return identidadFeignClient.clientePorUsuario(usuarioId);
        } catch (FeignException.NotFound e) {
            throw new SecurityException("El usuario no tiene un perfil de cliente");
        }
    }

    public Long vendedorIdDe(Long usuarioId) {
        try {
            return identidadFeignClient.vendedorPorUsuario(usuarioId).id();
        } catch (FeignException.NotFound e) {
            throw new SecurityException("El usuario no tiene un perfil de vendedor");
        }
    }

    public Map<Long, String> nombresClientes() {
        try {
            return identidadFeignClient.listarClientes().stream()
                    .collect(Collectors.toMap(ClienteResumenDTO::id, ClienteResumenDTO::nombreCompleto, (a, b) -> a));
        } catch (FeignException e) {
            log.warn("No se pudo consultar clientes en ms-identidad: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    public EstadisticasIdentidadDTO estadisticas() {
        try {
            return identidadFeignClient.estadisticas();
        } catch (FeignException e) {
            log.warn("No se pudo consultar estadísticas en ms-identidad: {}", e.getMessage());
            return new EstadisticasIdentidadDTO(0, 0, 0);
        }
    }
}
