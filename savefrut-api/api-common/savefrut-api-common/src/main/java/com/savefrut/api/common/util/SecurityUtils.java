package com.savefrut.api.common.util;

import com.savefrut.api.common.security.UserDetailsImpl;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;

/**
 * Utilidades para extraer información del contexto de seguridad.
 */
@UtilityClass
public class SecurityUtils {

    /**
     * Extrae el ID del usuario autenticado desde el objeto {@link Authentication}.
     *
     * @param authentication contexto de seguridad de la petición actual
     * @return ID del usuario autenticado
     */
    public static Long extraerUsuarioId(Authentication authentication) {
        return ((UserDetailsImpl) authentication.getPrincipal()).getId();
    }
}