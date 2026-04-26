package com.savefrut.api.common.security.service;

import com.savefrut.api.common.entity.Usuario;
import com.savefrut.api.common.repository.UsuarioRepository;
import com.savefrut.api.common.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Este servicio es el corazón de la autenticación.
 * Le dice a Spring Security CÓMO buscar un usuario en nuestra base de datos.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Este método es llamado por Spring Security cuando intenta autenticar a un usuario.
     * @param username El "username" que Spring recibe. En nuestro caso, será el EMAIL.
     */
    @Override
    @Transactional // Buena práctica para asegurar que los datos (como el Rol) se carguen
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        // Usamos el repositorio para buscar por email
        Usuario usuario = usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el email: " + username));

        // Convertimos nuestro Usuario (JPA) a un UserDetails (Spring Security)
        return new UserDetailsImpl(usuario);
    }
}