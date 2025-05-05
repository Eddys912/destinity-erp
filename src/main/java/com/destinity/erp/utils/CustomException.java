package com.destinity.erp.utils;

/**
 * Clase personalizada para el manejo centralizado de excepciones en el sistema.
 * Permite categorizar errores por tipo (base de datos, validación, negocio, etc.)
 * y lanzar excepciones claras con mensajes controlados en cada capa de la aplicación.
 */
public class CustomException extends RuntimeException {
    
    private final ExceptionType type;

    public CustomException(ExceptionType type, String message) {
        super(message);
        this.type = type;
    }

    /**
     * Enum que define los tipos de errores manejados en el sistema.
     * Facilita la clasificación de las excepciones para su interpretación y respuesta.
     */
    public enum ExceptionType {
        DATABASE_ERROR,
        DUPLICATED_KEY,
        VALIDATION_FAILED,
        NOT_FOUND,
        BUSINESS_RULE,
        INVALID_INPUT
    }
    
    public ExceptionType getType(){
        return type;
    }
    
    public static CustomException dbError(String message) {
        return new CustomException(ExceptionType.DATABASE_ERROR, message);
    }
    
    public static CustomException dbDuplicatedKey(String message) {
        return new CustomException(ExceptionType.DUPLICATED_KEY, message);
    }
    public static CustomException dbValidationFailed(String message) {
        return new CustomException(ExceptionType.VALIDATION_FAILED, message);
    }

    public static CustomException business(String message) {
        return new CustomException(ExceptionType.BUSINESS_RULE, message);
    }
    
    public static CustomException notFound(String message) {
        return new CustomException(ExceptionType.NOT_FOUND, message);
    }

    public static CustomException invalidInput(String property, String entity) {
        return new CustomException(ExceptionType.INVALID_INPUT, property + " del " + entity + " es requerido");
    }
}
