package com.savefrut.api.vendedor.service;

import com.savefrut.api.vendedor.dto.*;
import com.savefrut.api.common.entity.*;
import com.savefrut.api.common.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PerfilVendedorService {

    private final VendedorRepository vendedorRepository;
    private final DistritoRepository distritoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public Optional<PerfilVendedorDTO> getPerfil(Long usuarioId) {
        return vendedorRepository.findByUsuarioId(usuarioId)
                .map(this::toDTO);
    }

    @Transactional
    public Optional<PerfilVendedorDTO> actualizarPerfil(Long usuarioId, PerfilVendedorRequest request) {
        return vendedorRepository.findByUsuarioId(usuarioId)
                .map(vendedor -> {
                    if (request.direccion() != null) {
                        vendedor.setDireccion(request.direccion());
                    }
                    if (request.horario() != null) {
                        vendedor.setHorario(request.horario());
                    }
                    if (request.latitud() != null) {
                        vendedor.setLatitud(request.latitud());
                    }
                    if (request.longitud() != null) {
                        vendedor.setLongitud(request.longitud());
                    }
                    if (request.logoUrl() != null) {
                        vendedor.setLogoUrl(request.logoUrl());
                    }

                    log.info("Perfil actualizado para usuario {}", usuarioId);
                    return toDTO(vendedorRepository.save(vendedor));
                });
    }

    private PerfilVendedorDTO toDTO(Vendedor v) {
        return new PerfilVendedorDTO(
                v.getVendedorId(),
                v.getRuc(),
                v.getRazonSocial(),
                v.getDireccion(),
                v.getHorario(),
                v.getLatitud(),
                v.getLongitud(),
                v.getDistrito() != null ? v.getDistrito().getNombre() : null,
                v.getLogoUrl()
        );
    }
}