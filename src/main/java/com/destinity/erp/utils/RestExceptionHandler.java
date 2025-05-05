package com.destinity.erp.utils;

import jakarta.ws.rs.core.Response;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase utilitaria para manejar excepciones dentro de los servicios REST.
 * Permite mapear tipos de errores personalizados a códigos de estado HTTP
 * estándar y construir respuestas JSON consistentes, mejorando la trazabilidad
 * y el control de errores en la API. Incluye manejo tanto para excepciones
 * conocidas (definidas por el desarrollador) como para errores inesperados.
 */
public class RestExceptionHandler {

    private static final Logger LOGGER = Logger.getLogger(RestExceptionHandler.class.getName());

    /**
     * Mapea y obtiene el tipo de excepción
     *
     * @param type Tipo de excepción definido en la clase CustomException
     * @return Código HTTP correspondiente al tipo de excepción
     */
    private static Response.Status mapStatusFromType(CustomException.ExceptionType type) {
        return switch (type) {
            case INVALID_INPUT, VALIDATION_FAILED ->
                Response.Status.BAD_REQUEST;
            case NOT_FOUND ->
                Response.Status.NOT_FOUND;
            case BUSINESS_RULE ->
                Response.Status.CONFLICT;
            case DATABASE_ERROR, DUPLICATED_KEY ->
                Response.Status.INTERNAL_SERVER_ERROR;
            default ->
                Response.Status.INTERNAL_SERVER_ERROR;
        };
    }

    /**
     * Excepciones de base de datos y de negocio
     *
     * @param e excepción
     * @return respuesta con el mensaje y el tipo
     */
    public static Response handleCustomException(CustomException e) {
        LOGGER.log(Level.WARNING, "[{0}] {1}", new Object[]{e.getType(), e.getMessage()});
        return Response.status(mapStatusFromType(e.getType()))
                .entity(Map.of(
                        "message", e.getMessage(),
                        "type", e.getType().name()
                )).build();
    }

    /**
     * Excepción genérica
     *
     * @param e excepción
     * @return respuesta con el mensaje y el tipo
     */
    public static Response unexpectedCustomException(Exception e) {
        LOGGER.log(Level.SEVERE, "Error inesperado", e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of(
                        "message", "Error interno inesperado",
                        "type", "UNEXPECTED"
                )).build();
    }
}
