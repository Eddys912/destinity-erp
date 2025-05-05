/**
 * @file header_mobile_menu.js
 * @description Manejo del menú móvil para navegación responsiva.
 */

/**
 * Error personalizado para operaciones del menú móvil
 */
export class MobileMenuError extends Error {
  constructor(message) {
    super(message);
    this.name = 'MobileMenuError';
  }
}

/**
 * Inicializa el menú móvil configurando los eventos para mostrar/ocultar el menú.
 *
 * @throws {MobileMenuError} Si ocurre un error durante la inicialización del menú móvil.
 */
export function initializeMobileMenu() {
  try {
    const mobileButton = document.getElementById('mobile-menu-button');
    const mobileMenu = document.getElementById('mobile-menu');

    if (!mobileButton || !mobileMenu)
      throw new MobileMenuError('Elementos del menú móvil no encontrados en el DOM');

    mobileButton.addEventListener('click', () => {
      const isVisible = !mobileMenu.classList.contains('hidden');

      if (isVisible) mobileMenu.classList.add('hidden');
      mobileMenu.classList.remove('hidden');
    });

    // Configurar evento para cerrar automáticamente el menú móvil al hacer clic fuera
    document.addEventListener('click', (e) => {
      if (!mobileMenu.contains(e.target) && !mobileButton.contains(e.target))
        if (!mobileMenu.classList.contains('hidden')) mobileMenu.classList.add('hidden');
    });
  } catch (e) {
    if (e instanceof MobileMenuError) throw e;
    throw new MobileMenuError(`Error en la inicialización del menú móvil: ${e.message}`);
  }
}

document.addEventListener('DOMContentLoaded', initializeMobileMenu);
