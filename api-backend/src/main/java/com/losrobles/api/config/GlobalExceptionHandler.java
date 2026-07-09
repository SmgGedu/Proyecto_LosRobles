package com.losrobles.api.config;

import com.losrobles.api.models.ApiError;
import com.losrobles.api.util.FechaUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * Manejador global de excepciones de la API.
 * <p>
 * Convierte las excepciones lanzadas por controladores y servicios en
 * respuestas
 * HTTP consistentes con el formato {@link ApiError} (timestamp, status, error,
 * message, path), y registra en el log lo necesario para poder diagnosticar
 * problemas en el servidor.
 * <p>
 * Spring elige el handler según el tipo de excepción más específico, sin
 * importar
 * el orden de declaración de los métodos.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Errores de autorización (@PreAuthorize, etc.) -> 403, sin exponer detalles
     * internos.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Acceso denegado en {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError error = new ApiError(
                FechaUtils.ahora(),
                HttpStatus.FORBIDDEN.value(),
                "Acceso Denegado",
                "No tienes permisos para realizar esta acción.",
                request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    /**
     * Errores de lógica de negocio lanzados por los servicios -> 400, con el
     * mensaje original.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        log.warn("Error de negocio en {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError error = new ApiError(
                FechaUtils.ahora(),
                HttpStatus.BAD_REQUEST.value(),
                "Error de Negocio",
                ex.getMessage(),
                request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Recurso no encontrado -> 404. Se usa cuando un servicio lanza
     * {@link IllegalArgumentException} para indicar que el ID/recurso buscado no
     * existe.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleNotFound(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Recurso no encontrado en {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError error = new ApiError(
                FechaUtils.ahora(),
                HttpStatus.NOT_FOUND.value(),
                "No Encontrado",
                ex.getMessage(),
                request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /** Cuerpo del request ilegible (JSON inválido o vacío) -> 400. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMessageNotReadable(HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        log.warn("Cuerpo de request inválido en {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError error = new ApiError(
                FechaUtils.ahora(),
                HttpStatus.BAD_REQUEST.value(),
                "Solicitud Inválida",
                "El cuerpo de la solicitud es inválido o está mal formado.",
                request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /** Falta un parámetro requerido en el request (@RequestParam) -> 400. */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParam(MissingServletRequestParameterException ex,
            HttpServletRequest request) {
        log.warn("Parámetro faltante en {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError error = new ApiError(
                FechaUtils.ahora(),
                HttpStatus.BAD_REQUEST.value(),
                "Solicitud Inválida",
                ex.getMessage(),
                request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Un parámetro o variable de ruta tiene un tipo de dato incorrecto (ej. texto
     * donde se espera un ID numérico) -> 400.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        log.warn("Tipo de dato inválido en {}: {}", request.getRequestURI(), ex.getMessage());
        String mensaje = "El parámetro '" + ex.getName() + "' tiene un valor inválido: '" + ex.getValue() + "'.";
        ApiError error = new ApiError(
                FechaUtils.ahora(),
                HttpStatus.BAD_REQUEST.value(),
                "Solicitud Inválida",
                mensaje,
                request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Falla la validación de un @RequestBody anotado con @Valid -> 400, con el
     * detalle por campo.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Error de validación en {}: {}", request.getRequestURI(), mensaje);
        ApiError error = new ApiError(
                FechaUtils.ahora(),
                HttpStatus.BAD_REQUEST.value(),
                "Solicitud Inválida",
                mensaje,
                request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Se llamó a un endpoint existente con un método HTTP no soportado (ej. DELETE
     * en vez de POST) -> 405.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {
        log.warn("Método no soportado en {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError error = new ApiError(
                FechaUtils.ahora(),
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                "Método No Soportado",
                ex.getMessage(),
                request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Catch-all de seguridad para cualquier error no controlado -> 500, sin exponer
     * detalles al cliente.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGlobalException(Exception ex, HttpServletRequest request) {
        log.error("Error inesperado en {}", request.getRequestURI(), ex);
        ApiError error = new ApiError(
                FechaUtils.ahora(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error Interno del Servidor",
                "Ha ocurrido un error inesperado. Por favor, contacte al administrador.",
                request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
