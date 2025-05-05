package com.destinity.erp.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Clase utilitaria que proporciona métodos para convertir entre tipos de fecha y hora.
 * Interopera entre la API moderna de Java y bibliotecas como MongoDB.
 */
public class ToDate {

    /**
     * Convierte LocalDateTime a Date, usando la zona horaria del sistema.
     * @param dateTime Objeto LocalDateTime a convertir
     * @return Objeto Date equivalente o null si la entrada es null
     */
    public static Date toDate(LocalDateTime dateTime) {
        return dateTime != null
                ? Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant())
                : null;
    }

    /**
     * Convierte un Date a LocalDateTime, usando la zona horaria del sistema.
     * @param date Objeto Date a convertir
     * @return Objeto LocalDateTime equivalente o null si la entrada es null
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        return date != null
                ? date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                : null;
    }
}
