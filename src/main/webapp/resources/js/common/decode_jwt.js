/**
 * @file decode_JWT
 * @description Utilidad para decodificar tokens JWT (JSON Web Tokens).
 */

/**
 * Error personalizado para operaciones con JWT
 */
export class JWTDecodeError extends Error {
  constructor(message) {
    super(message);
    this.name = 'JWTDecodeError';
  }
}

/**
 * Decodifica un token JWT para obtener los claims del payload.
 * Esta función NO verifica la firma del token, solo extrae la información.
 *
 * @param {string} token - El token JWT a decodificar.
 * @returns {Object|null} Objeto con los datos del payload si es válido, o null si hay error.
 * @throws {JWTDecodeError} Si ocurre un error durante la decodificación.
 */
export function decodeJWT(token) {
  if (!token) return null;

  const parts = token.split('.');

  if (parts.length !== 3) throw new JWTDecodeError('Token mal formado');

  try {
    const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
    const decodedData = atob(base64);
    const jsonString = decodeURIComponent(
      decodedData
        .split('')
        .map(function (c) {
          return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        })
        .join('')
    );
    return JSON.parse(jsonString);
  } catch (e) {
    throw new JWTDecodeError(`Error al decodificar el token: ${e.message}`);
  }
}
