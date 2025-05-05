package com.destinity.erp.auth;

import com.destinity.erp.hr.UserModel;
import com.destinity.erp.hr.UserRepository;
import com.destinity.erp.utils.CustomException;
import com.destinity.erp.utils.JwtUtil;
import com.destinity.erp.utils.PasswordHasher;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servicio encargado de manejar la lógica de negocio del inicio de sesión.
 * Verifica credenciales del usuario, genera el token JWT y encapsula la
 * respuesta para el controlador.
 */
@ApplicationScoped
public class AuthService {

    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());

    @Inject
    private UserRepository userRepository;

    /**
     * Crea un token JWT si las credenciales son válidas.
     *
     * @param email correo electrónico proporcionado por el usuario
     * @param password contraseña proporcionada por el usuario
     * @return Mapa con clave "token" que contiene el JWT generado
     * @throws CustomException si las credenciales son inválidas
     */
    public Map<String, String> login(String email, String password) {
        Optional<UserModel> user = userRepository.findUserByEmail(email);

        if (user.isEmpty() || !PasswordHasher.checkPassword(password, user.get().getPassword())) {
            LOGGER.log(Level.WARNING, "Credenciales inválidas, usuario con correo: {0}", email);
            throw CustomException.notFound("Credenciales inválidas, correo o contraseña incorrectas");
        }

        LOGGER.log(Level.INFO, "Usuario encontrado: {0}", user.get().getId());

        String token = JwtUtil.generateToken(
                user.get().getId().toHexString(),
                user.get().getEmployeeData().getRole(),
                user.get().getEmail(),
                user.get().getFirstName() + " " + user.get().getLastName() + " " + user.get().getMiddleName());

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return response;
    }
}
