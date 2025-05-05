package com.destinity.erp.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Clase utilitaria para el manejo seguro de contraseñas usando BCrypt.
 *
 * BCrypt para hashing con sal incorporada, lo que protege contra ataques por
 * diccionario y rainbow tables usada para almacenar contraseñas.
 */
public class PasswordHasher {

    /**
     * Hashea una contraseña en texto plano utilizando BCrypt con una sal.
     *
     * @param plainPassword contraseña en texto plano ingresada por el usuario.
     * @return hash de la contraseña, seguro para almacenar en la base de datos.
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    /**
     * Verifica si la contraseña ingresada coincide con el hash almacenado.
     *
     * @param plainPassword contraseña en texto plano proporcionada en el login.
     * @param hashedPassword hash almacenado en la base de datos.
     * @return true si coinciden, false en caso contrario.
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
