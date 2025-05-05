package com.destinity.erp.auth;

/**
 * DTO para transportar las credenciales del usuario desde el frontend.
 * Este objeto encapsula el correo y contraseña proporcionados por el
 * usuario en el inicio de sesión, utilizado solo como contenedor de datos.
 */
public class AuthDTO {

    public String email;
    public String password;
}
