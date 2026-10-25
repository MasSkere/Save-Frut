package com.savefrut.msidentidad.util;

import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

@UtilityClass
public class CookieUtils {

    private static final String COOKIE_PATH          = "/";
    private static final int    REFRESH_MAX_AGE_SECS = 7 * 24 * 60 * 60;

    public static void agregarCookieSegura(HttpServletResponse response, String nombre, String valor) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie(nombre, valor, REFRESH_MAX_AGE_SECS));
    }

    public static void expirarCookie(HttpServletResponse response, String nombre) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie(nombre, "", 0));
    }

    /**
     * SameSite=None: el frontend publicado (p. ej. Vercel) está en otro sitio que el API Gateway,
     * y con el valor por defecto (Lax) el navegador no enviaría la cookie de refresh.
     */
    private static String buildCookie(String nombre, String valor, int maxAge) {
        return ResponseCookie.from(nombre, valor)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path(COOKIE_PATH)
                .maxAge(maxAge)
                .build()
                .toString();
    }
}
