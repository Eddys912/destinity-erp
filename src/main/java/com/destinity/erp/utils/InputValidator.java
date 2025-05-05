package com.destinity.erp.utils;

import java.util.regex.Pattern;

/**
 * Clase utilitaria para validar entradas de texto en el sistema.
 * Aplica reglas básicas como no permitir campos vacíos y evitar caracteres especiales no permitidos,
 * protegiendo así la integridad de los datos recibidos desde formularios o peticiones externas.
 */
public class InputValidator {
    
    private static final Pattern SAFE_TEXT_PATTERN = Pattern.compile("^[\\p{L}\\p{N}\\s.,;:!¡¿?()\\-_'@+%#=/]*$");
    
    /**
     * Lanza una excepción de tipo BUSINESS_RULE en caso de error.
     * @param input Texto a validar
     * @param fieldName Nombre del campo que se está validando (usado en el mensaje de error)
     * @return true si la entrada es válida
     */
    public static boolean isNotEmpty(String input, String fieldName){
        if (input == null || input.isBlank()) {
            throw CustomException.business("El campo " + fieldName + " no puede estar vacio");
        }
        if (!SAFE_TEXT_PATTERN.matcher(input).matches()) {
            throw CustomException.business("El campo " + fieldName + " contiene carácteres no válidos");
        }
        return true;
    }
}
