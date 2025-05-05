package com.destinity.erp.auth;

import com.destinity.erp.utils.CustomException;
import com.destinity.erp.utils.RestExceptionHandler;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

/**
 * Controlador REST encargado de las peticiones con la autenticación.
 *  Expone el endpoint del API de autenticación y devuelve un token.
 */
@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthController {

    @Inject
    private AuthService authService;

    /**
     * Inicia sesión con las credenciales proporcionadas.
     *
     * @param credentials Objeto con email y contraseña del usuario
     * @return token JWT si es exitoso, o mensaje de error si no son inválidas
     */
    @POST
    @Path("/login")
    public Response login(AuthDTO credentials) {
        try {
            Map<String, String> token = authService.login(credentials.email, credentials.password);
            return Response.ok(token).build();
        } catch (CustomException ex) {
            return RestExceptionHandler.handleCustomException(ex);
        } catch (Exception e) {
            return RestExceptionHandler.unexpectedCustomException(e);
        }
    }
}
