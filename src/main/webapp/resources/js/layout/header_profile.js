/**
 * @file header_profile.js
 * @description Manejo del menú de perfil de usuario en el encabezado.
 */

import { decodeJWT } from '../common/decode_jwt.js';

/**
 * Error personalizado para operaciones del perfil de usuario
 */
export class ProfileError extends Error {
  constructor(message) {
    super(message);
    this.name = 'ProfileError';
  }
}

/**
 * Inicializa el menú de perfil de usuario mostrando la información del token JWT
 * y configurando los eventos para mostrar/ocultar el menú.
 *
 * @throws {ProfileError} Si ocurre un error durante la inicialización del perfil.
 */
export function initializeProfileMenu() {
  try {
    const profileButton = document.getElementById('profile-button');
    const profileMenu = document.getElementById('profile-menu');

    if (!profileButton || !profileMenu)
      throw new ProfileError('Elementos del perfil no encontrados en el DOM');

    const nameEl = profileMenu.querySelector('.profile-user');
    const emailEl = profileMenu.querySelector('.profile-email');

    if (!nameEl || !emailEl)
      throw new ProfileError('Elementos para mostrar información del usuario no encontrados');

    const token = sessionStorage.getItem('authToken');
    let userData = null;

    try {
      userData = decodeJWT(token);
    } catch (error) {
      throw new ProfileError(`Error al decodificar el token: ${error.message}`);
    }

    if (userData) {
      nameEl.textContent = userData.name || 'Desconocido';
      emailEl.textContent = userData.email || 'Sin correo';
    } else {
      window.location.href = '/destinity-erp';
      throw new ProfileError('Sesión no válida. Redirigiendo al login');
    }

    profileButton.addEventListener('click', (e) => {
      e.stopPropagation();
      profileMenu.classList.toggle('opacity-0');
      profileMenu.classList.toggle('scale-95');
      profileMenu.classList.toggle('pointer-events-none');
    });

    // Configurar evento para ocultar menú al hacer clic fuera
    document.addEventListener('click', (e) => {
      if (!profileMenu.contains(e.target) && !profileButton.contains(e.target)) {
        if (!profileMenu.classList.contains('pointer-events-none'))
          profileMenu.classList.add('opacity-0', 'scale-95', 'pointer-events-none');
      }
    });
  } catch (e) {
    if (e instanceof ProfileError) throw e;
    throw new ProfileError(`Error en la inicialización del perfil: ${e.message}`);
  }
}

document.addEventListener('DOMContentLoaded', initializeProfileMenu);
