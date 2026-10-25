package com.savefrut.msidentidad.service;

import com.savefrut.msidentidad.dto.admin.UsuarioAdminDTO;
import com.savefrut.msidentidad.entity.Usuario;
import com.savefrut.msidentidad.repository.UsuarioRepository;
import com.savefrut.msidentidad.repository.ClienteRepository;
import com.savefrut.msidentidad.repository.VendedorRepository;
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

    private UsuarioAdminDTO toDTO(Usuario u) {
        return new UsuarioAdminDTO(
                u.getId(),
                u.getEmail(),
                u.getRol().getNombre(),
                u.getEstado(),
                u.getFechaRegistro()
        );
    }
}
