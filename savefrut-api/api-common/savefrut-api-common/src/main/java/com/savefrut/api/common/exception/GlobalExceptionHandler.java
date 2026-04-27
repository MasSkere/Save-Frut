package com.savefrut.api.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PSQLException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para toda la API.
 *
 * Ubicado en módulo COMMON para ser heredado por todos los módulos hijos
 * (client, admin, etc) automáticamente.
 *
 * Características:
 * - Captura excepciones de PostgreSQL y las convierte a mensajes claros
 * - HTTP 409 (CONFLICT) para errores de stock insuficiente
 * - Estructura JSON consistente
 * - Logging centralizado
 * - Mensajes amigables al usuario
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones de PostgreSQL, convirtiendo mensajes de error técnicos
     * a mensajes claros para el cliente.
     *
     * Casos comunes:
     * - Stock insuficiente → HTTP 409
     * - Constraint violations → HTTP 409
     * - Otros errores SQL → HTTP 409
     *
     * @param ex PSQLException lanzada por PostgreSQL
     * @param request Contexto de la solicitud
     * @return ResponseEntity con estructura de error JSON
     */
    @ExceptionHandler(PSQLException.class)
    public ResponseEntity<Map<String, Object>> handlePSQLException(
            PSQLException ex, WebRequest request) {

        log.error("Error de PostgreSQL: {}", ex.getMessage(), ex);

        Map<String, Object> body = createErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Error en la base de datos",
                extractPSQLErrorMessage(ex)
        );

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    /**
     * Maneja errores de validación de stock y requerimientos de negocio.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalStateException(
            IllegalStateException ex, WebRequest request) {

        log.warn("Error de estado ilegal: {}", ex.getMessage());

        Map<String, Object> body = createErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Stock insuficiente o error de validación",
                ex.getMessage()
        );

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja errores de argumentos inválidos.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        log.warn("Argumento inválido: {}", ex.getMessage());

        Map<String, Object> body = createErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Parámetro inválido",
                ex.getMessage()
        );

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja excepciones de usuario no encontrado.
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUsernameNotFoundException(
            UsernameNotFoundException ex, WebRequest request) {

        log.warn("Usuario no encontrado: {}", ex.getMessage());

        Map<String, Object> body = createErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Usuario o cliente no encontrado",
                ex.getMessage()
        );

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    /**
     * Maneja excepciones de acceso denegado a recursos protegidos.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {

        log.warn("Acceso denegado: {}", ex.getMessage());

        Map<String, Object> body = createErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Acceso denegado",
                "No tienes permisos para acceder a este recurso"
        );

        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    /**
     * Maneja excepciones de seguridad personalizadas.
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> handleSecurityException(
            SecurityException ex, WebRequest request) {

        log.warn("Error de seguridad: {}", ex.getMessage());

        Map<String, Object> body = createErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Error de seguridad",
                ex.getMessage()
        );

        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    /**
     * Maneja RuntimeException genérica como último recurso.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex, WebRequest request) {

        log.error("Error interno del servidor: {}", ex.getMessage(), ex);

        Map<String, Object> body = createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno del servidor",
                "Ha ocurrido un error inesperado. Por favor, intenta nuevamente más tarde."
        );

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Maneja excepciones genéricas como último recurso absoluto.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(
            Exception ex, WebRequest request) {

        log.error("Excepción no manejada: {}", ex.getMessage(), ex);

        Map<String, Object> body = createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error inesperado",
                "Ha ocurrido un error inesperado. Por favor, contacta con soporte."
        );

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Extrae el mensaje de error legible desde una excepción de PostgreSQL.
     *
     * Identifica errores específicos (stock, duplicados, etc) y convierte
     * el mensaje técnico a uno amigable para el usuario.
     *
     * @param ex PSQLException
     * @return String con el mensaje de error limpio y comprensible
     */
    private String extractPSQLErrorMessage(PSQLException ex) {
        String message = ex.getMessage();

        if (message != null) {
            if (message.contains("Stock insuficiente")) {
                return "Stock insuficiente para uno o más productos en tu orden.";
            }
            if (message.contains("no encontrado")) {
                return "Producto no encontrado en el catálogo.";
            }
            if (message.contains("violates unique constraint")) {
                return "Duplicate entry: Este registro ya existe.";
            }
            if (message.contains("violates foreign key constraint")) {
                return "Error de integridad: recurso referenciado no existe.";
            }
        }

        return "Error en la base de datos. Por favor, intenta nuevamente.";
    }

    /**
     * Crea una estructura de respuesta de error consistente.
     *
     * @param status  Código HTTP de estado
     * @param error   Mensaje de error general (título)
     * @param message Mensaje de error detallado (descripción)
     * @return Map con la estructura de error JSON
     */
    private Map<String, Object> createErrorResponse(Integer status, String error, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status);
        body.put("error", error);
        body.put("message", message);
        return body;
    }
}

