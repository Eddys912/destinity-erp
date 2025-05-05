package com.destinity.erp.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase utilitaria para la generación y validación de tokens JWT.
 *  Utiliza el algoritmo HMAC256 y contiene claims personalizados.
 */
public class JwtUtil {

    private static final String SECRET_KEY;
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 8;
    private static final Algorithm algorithm;

    static {
        SECRET_KEY = new EnvReader().getProperty("SECRET_KEY");
        if (SECRET_KEY == null || SECRET_KEY.isBlank()) {
            throw new IllegalStateException("SECRET_KEY no definido en env.properties");
        }
        algorithm = Algorithm.HMAC256(SECRET_KEY);
    }

    /**
     * Genera un token JWT con los datos del usuario.
     *
     * @param userId ID del usuario
     * @param role rol del usuario
     * @param email correo electrónico del usuario
     * @param name nombre completo del usuario
     * @return token JWT generado
     */
    public static String generateToken(String userId, String role, String email, String name) {
        return JWT.create()
                .withSubject(userId)
                .withClaim("role", role)
                .withClaim("email", email)
                .withClaim("name", name)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(algorithm);
    }

    /**
     * Verifica y decodifica un token JWT.
     *
     * @param token el token JWT recibido
     * @return objeto DecodedJWT con los claims del token
     * @throws RuntimeException si el token es inválido o no puede verificarse
     */
    public static DecodedJWT verifyToken(String token) {
        JWTVerifier verifier = JWT.require(algorithm).build();
        return verifier.verify(token);
    }

    /**
     * Obtiene el ID del usuario a partir del token JWT.
     *
     * @param token token JWT
     * @return ID del usuario
     */
    public static String getUserIdFromToken(String token) {
        return verifyToken(token).getSubject();
    }

    /**
     * Obtiene el rol del usuario desde el token JWT.
     *
     * @param token token JWT
     * @return rol del usuario
     */
    public static String getRoleFromToken(String token) {
        return verifyToken(token).getClaim("role").asString();
    }

    /**
     * Obtiene el correo electrónico del usuario desde el token JWT.
     *
     * @param token token JWT
     * @return email del usuario
     */
    public static String getEmailFromToken(String token) {
        return verifyToken(token).getClaim("email").asString();
    }

    /**
     * Obtiene el nombre del usuario desde el token JWT.
     *
     * @param token token JWT
     * @return nombre completo del usuario
     */
    public static String getNameFromToken(String token) {
        return verifyToken(token).getClaim("name").asString();
    }

    /**
     * Retorna todos los claims relevantes del token en un mapa.
     *
     * @param token token JWT
     * @return mapa con userId, role, email y name
     */
    public static Map<String, String> getAllClaims(String token) {
        DecodedJWT jwt = verifyToken(token);
        Map<String, String> claims = new HashMap<>();
        claims.put("userId", jwt.getSubject());
        claims.put("role", jwt.getClaim("role").asString());
        claims.put("email", jwt.getClaim("email").asString());
        claims.put("name", jwt.getClaim("name").asString());
        return claims;
    }
}
