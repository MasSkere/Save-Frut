package com.savefrut.api.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;

/**
 * Utilidades para gestionar cookies HTTP en las respuestas.
 *
 * <p>Centraliza la configuración de seguridad de cookies (HttpOnly, Secure, Path)
 * para evitar inconsistencias entre controladores.
 */
@UtilityClass
public class CookieUtils {

    private static final String COOKIE_PATH          = "/";
    private static final int    REFRESH_MAX_AGE_SECS = 7 * 24 * 60 * 60; // 7 días

    /**
     * Agrega una cookie HttpOnly segura con el tiempo de vida del refresh token.
     *
     * @param response respuesta HTTP donde se agrega la cookie
     * @param nombre   nombre de la cookie
     * @param valor    valor a almacenar
     */
    public static void agregarCookieSegura(HttpServletResponse response, String nombre, String valor) {
        response.addCookie(buildCookie(nombre, valor, REFRESH_MAX_AGE_SECS));
    }

    /**
     * Expira una cookie existente estableciendo su {@code maxAge} a 0.
     *
     * @param response respuesta HTTP donde se expira la cookie
     * @param nombre   nombre de la cookie a expirar
     */
    public static void expirarCookie(HttpServletResponse response, String nombre) {
        response.addCookie(buildCookie(nombre, "", 0));
    }

    // ─── Builder interno ───────────────────────────────────────────────────────

    private static Cookie buildCookie(String nombre, String valor, int maxAge) {
        Cookie cookie = new Cookie(nombre, valor);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath(COOKIE_PATH);
        cookie.setMaxAge(maxAge);
        return cookie;
    }
}