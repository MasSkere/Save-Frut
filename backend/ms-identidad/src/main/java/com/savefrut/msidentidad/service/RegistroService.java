package com.savefrut.msidentidad.service;

import com.savefrut.msidentidad.dto.RegistroRequest;
import com.savefrut.msidentidad.entity.Cliente;
import com.savefrut.msidentidad.entity.Rol;
import com.savefrut.msidentidad.entity.Usuario;
import com.savefrut.msidentidad.entity.Vendedor;
import com.savefrut.msidentidad.repository.ClienteRepository;
import com.savefrut.msidentidad.repository.DistritoRepository;
import com.savefrut.msidentidad.repository.RolRepository;
import com.savefrut.msidentidad.repository.UsuarioRepository;
import com.savefrut.msidentidad.repository.VendedorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistroService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final ClienteRepository clienteRepository;
    private final VendedorRepository vendedorRepository;
    private final DistritoRepository distritoRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Usuario registrar(RegistroRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new IllegalStateException("El email ya se encuentra registrado");
        }

        boolean esVendedor = "VENDEDOR".equals(request.rol());
        validarDatosPorRol(request, esVendedor);

        Rol rol = rolRepository.findByNombre("ROLE_" + request.rol())
                .orElseThrow(() -> new IllegalArgumentException("Rol no válido: " + request.rol()));

        Usuario usuario = new Usuario();
        usuario.setEmail(request.email());
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setRol(rol);
        usuario.setEstado("ACTIVO");
        usuario = usuarioRepository.save(usuario);

        if (esVendedor) {
            crearVendedor(usuario, request);
        } else {
            crearCliente(usuario, request);
        }

        log.info("Usuario {} registrado con rol {}", usuario.getId(), request.rol());
        return usuario;
    }

    private void validarDatosPorRol(RegistroRequest request, boolean esVendedor) {
        if (esVendedor) {
            if (!StringUtils.hasText(request.ruc()) || !request.ruc().matches("[0-9]{11}")) {
                throw new IllegalArgumentException("El RUC debe tener 11 dígitos");
            }
            if (!StringUtils.hasText(request.razonSocial())) {
                throw new IllegalArgumentException("La razón social es obligatoria");
            }
        } else if (!StringUtils.hasText(request.nombres()) || !StringUtils.hasText(request.apellidos())) {
            throw new IllegalArgumentException("Nombres y apellidos son obligatorios");
        }
    }

    private void crearCliente(Usuario usuario, RegistroRequest request) {
        Cliente cliente = new Cliente();
        cliente.setUsuario(usuario);
        cliente.setNombres(request.nombres());
        cliente.setApellidos(request.apellidos());
        cliente.setTelefono(request.telefono());
        cliente.setDireccion(request.direccion());
        if (request.distritoId() != null) {
            distritoRepository.findById(request.distritoId()).ifPresent(cliente::setDistrito);
        }
        clienteRepository.save(cliente);
    }

    private void crearVendedor(Usuario usuario, RegistroRequest request) {
        Vendedor vendedor = new Vendedor();
        vendedor.setUsuario(usuario);
        vendedor.setRuc(request.ruc());
        vendedor.setRazonSocial(request.razonSocial());
        vendedor.setDireccion(request.direccion());
        vendedor.setAprobado(false);
        if (request.distritoId() != null) {
            distritoRepository.findById(request.distritoId()).ifPresent(vendedor::setDistrito);
        }
        vendedorRepository.save(vendedor);
    }
}
