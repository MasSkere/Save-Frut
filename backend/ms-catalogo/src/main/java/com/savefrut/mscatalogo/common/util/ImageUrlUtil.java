package com.savefrut.mscatalogo.common.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utilidad para gestionar URLs de imágenes con soporte a placeholder.
 *
 * Características:
 * - Valida que las URLs de imágenes sean válidas
 * - Proporciona una imagen placeholder por defecto si no hay imagen
 * - No guarda imágenes como BLOB, solo URLs
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImageUrlUtil {

    @Value("${app.image.placeholder-url:https://placehold.co/400x400/cccccc/999999?text=Sin+Imagen}")
    private String placeholderUrl;

    /**
     * Obtiene una URL de imagen válida o retorna el placeholder.
     *
     * @param imagenUrl URL de la imagen
     * @return URL válida o placeholder
     */
    public String getValidImageUrl(String imagenUrl) {
        if (imagenUrl == null || imagenUrl.isBlank()) {
            log.debug("Imagen nula o vacía, usando placeholder");
            return placeholderUrl;
        }

        // Validar que sea una URL válida
        if (isValidUrl(imagenUrl)) {
            return imagenUrl;
        }

        log.warn("URL de imagen inválida: {}. Usando placeholder", imagenUrl);
        return placeholderUrl;
    }

    /**
     * Valida si una URL es válida.
     *
     * @param urlString URL a validar
     * @return true si es válida, false en caso contrario
     */
    private boolean isValidUrl(String urlString) {
        try {
            new java.net.URL(urlString);
            return true;
        } catch (java.net.MalformedURLException e) {
            log.debug("URL inválida: {}", urlString, e);
            return false;
        }
    }

    /**
     * Procesa una lista de objetos con URL de imagen.
     *
     * @param imagenUrl URL a procesar
     * @return URL válida o placeholder
     */
    public String getOrDefault(String imagenUrl) {
        return getValidImageUrl(imagenUrl);
    }
}

