package com.savefrut.msidentidad.util;

import com.savefrut.msidentidad.security.UserDetailsImpl;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;

@UtilityClass
public class SecurityUtils {

    public static Long extraerUsuarioId(Authentication authentication) {
        return ((UserDetailsImpl) authentication.getPrincipal()).getId();
    }
}
