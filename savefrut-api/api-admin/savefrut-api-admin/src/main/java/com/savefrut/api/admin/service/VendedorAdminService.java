package com.savefrut.api.admin.service;

import com.savefrut.api.admin.dto.VendedorAdminDTO;
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
public class VendedorAdminService {

    private final VendedorRepository vendedorRepository;

    @Transactional(readOnly = true)
    public List<VendedorAdminDTO> getVendedoresPendientes() {
        return vendedorRepository.findAll().stream()
                .filter(v -> !Boolean.TRUE.equals(v.getAprobado()))
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public VendedorAdminDTO getVendedorById(Long id) {
        return vendedorRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Vendedor no encontrado"));
    }

    @Transactional
    public VendedorAdminDTO cambiarEstadoAprobacion(Long id, Boolean aprobado, String razon) {
        return vendedorRepository.findById(id)
                .map(vendedor -> {
                    vendedor.setAprobado(aprobado);
                    log.info("Vendedor {} aprobado={}. Razón: {}", id, aprobado, razon);
                    return toDTO(vendedorRepository.save(vendedor));
                })
                .orElseThrow(() -> new IllegalArgumentException("Vendedor no encontrado"));
    }

    private VendedorAdminDTO toDTO(Vendedor v) {
        return new VendedorAdminDTO(
                v.getVendedorId(),
                v.getRuc(),
                v.getRazonSocial(),
                v.getDireccion(),
                v.getHorario(),
                v.getLatitud(),
                v.getLongitud(),
                v.getDistrito() != null ? v.getDistrito().getNombre() : null,
                v.getAprobado(),
                v.getUsuario().getId(),
                v.getUsuario().getEmail()
        );
    }
}