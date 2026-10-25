package com.savefrut.mscatalogo.service;

import com.savefrut.mscatalogo.dto.VendedorResumenDTO;
import com.savefrut.mscatalogo.feign.IdentidadFeignClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendedorConsultaService {

    private final IdentidadFeignClient identidadFeignClient;

    public Optional<VendedorResumenDTO> getVendedorPorUsuario(Long usuarioId) {
        try {
            return Optional.ofNullable(identidadFeignClient.obtenerPorUsuario(usuarioId));
        } catch (FeignException.NotFound e) {
            return Optional.empty();
        }
    }

    /**
     * Tiendas aprobadas por el administrador (id -> razón social). Solo sus productos son públicos.
     * Devuelve null si ms-identidad no responde, para no ocultar todo el catálogo por una caída.
     */
    public Map<Long, String> getVendedoresAprobados() {
        try {
            return identidadFeignClient.listarVendedores().stream()
                    .filter(v -> Boolean.TRUE.equals(v.aprobado()))
                    .collect(Collectors.toMap(VendedorResumenDTO::id, VendedorResumenDTO::razonSocial, (a, b) -> a));
        } catch (FeignException e) {
            log.warn("No se pudo consultar vendedores en ms-identidad: {}", e.getMessage());
            return null;
        }
    }

    public Map<Long, String> getNombresVendedores() {
        try {
            return identidadFeignClient.listarVendedores().stream()
                    .collect(Collectors.toMap(VendedorResumenDTO::id, VendedorResumenDTO::razonSocial, (a, b) -> a));
        } catch (FeignException e) {
            log.warn("No se pudo consultar vendedores en ms-identidad: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}
