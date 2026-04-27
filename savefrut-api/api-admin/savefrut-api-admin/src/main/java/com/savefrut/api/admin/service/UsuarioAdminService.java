package com.savefrut.api.admin.service;

import com.savefrut.api.admin.dto.UsuarioAdminDTO;
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
public class UsuarioAdminService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final VendedorRepository vendedorRepository;
    private final OrdenRepository ordenRepository;

    @Transactional(readOnly = true)
    public List<UsuarioAdminDTO> getUsuarios(String tipo) {
        return usuarioRepository.findAll().stream()
                .filter(u -> tipo == null || u.getRol().getNombre().equalsIgnoreCase("ROLE_" + tipo.toUpperCase()))
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioAdminDTO getUsuarioById(Long id) {
        return usuarioRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    @Transactional
    public UsuarioAdminDTO cambiarEstadoUsuario(Long id, String nuevoEstado, String razon) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    usuario.setEstado(nuevoEstado);
                    log.info("Usuario {} cambió a estado {}. Razón: {}", id, nuevoEstado, razon);
                    return toDTO(usuarioRepository.save(usuario));
                })
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getHistorial(Long id) {
        long totalCompras = clienteRepository.findByUsuarioId(id)
                .map(cliente -> ordenRepository.countByClienteId(cliente.getClienteId()))
                .orElse(0L);

        return java.util.Map.of("totalCompras", totalCompras);
    }

    private UsuarioAdminDTO toDTO(Usuario u) {
        Cliente c = clienteRepository.findByUsuarioId(u.getId()).orElse(null);
        Vendedor v = vendedorRepository.findByUsuarioId(u.getId()).orElse(null);

        return new UsuarioAdminDTO(
                u.getId(),
                u.getEmail(),
                u.getRol().getNombre(),
                u.getEstado(),
                u.getFechaRegistro(),
                c != null ? c.getClienteId() : null,
                c != null ? c.getNombres() : null,
                c != null ? c.getApellidos() : null,
                v != null ? v.getVendedorId() : null,
                v != null ? v.getRazonSocial() : null
        );
    }
}