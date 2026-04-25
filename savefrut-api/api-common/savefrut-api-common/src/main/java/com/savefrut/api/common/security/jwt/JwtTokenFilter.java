package com.savefrut.api.common.security.jwt;

import com.savefrut.api.common.security.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

/**
 * Este es el "guardia de seguridad" de nuestra API.
 * Se ejecuta UNA VEZ por cada petición para verificar el token JWT.
 * Ahora lee tokens tanto de headers como de cookies.
 */
public class JwtTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenFilter.class);

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    /**
     * El método principal del filtro. Aquí ocurre toda la magia.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. Extraemos el token del encabezado "Authorization" o de la cookie "token"
            String jwt = parseJwt(request);

            // 2. Si hay un token y es válido...
            if (jwt != null && jwtTokenProvider.validateToken(jwt)) {

                // 3. ...obtenemos el email (username) del token
                String username = jwtTokenProvider.getUsernameFromToken(jwt);

                // 4. ...cargamos los detalles del usuario desde la base de datos
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 5. ...creamos un objeto de autenticación
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 6. ...y lo establecemos en el contexto de seguridad de Spring.
                // ¡A partir de aquí, el usuario está "autenticado" para esta petición!
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("No se pudo establecer la autenticación del usuario: {}", e.getMessage());
        }

        // 7. Continuamos con el resto de los filtros de la cadena.
        filterChain.doFilter(request, response);
    }

    /**
     * Método auxiliar para extraer el token del encabezado "Authorization" o de la cookie "token".
     * Prioridad: Header > Cookie
     */
    private String parseJwt(HttpServletRequest request) {
        // Primero intentar desde header
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        // Si no hay en header, intentar desde cookie
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> "token".equals(cookie.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
        }

        return null;
    }
}