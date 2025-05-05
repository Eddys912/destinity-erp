/**
 * @file header_role.js
 * @description Renderiza el menú de navegación según el rol del usuario.
 */

import { decodeJWT } from '../common/decode_jwt.js';

/**
 * Error personalizado para operaciones de renderizado del menú
 */
export class MenuRenderError extends Error {
  constructor(message) {
    super(message);
    this.name = 'MenuRenderError';
  }
}

/**
 * Estructura de menús disponibles según el rol del usuario.
 */
const BASE_URL = window.location.origin + '/destinity-erp';
const menuItems = {
  Administrador: [
    { name: 'Inicio', path: BASE_URL + '/pages/home.xhtml' },
    { name: 'Analíticas', path: BASE_URL + '/pages/analytics.xhtml' },
    { name: 'Inventario', path: BASE_URL + '/pages/inventory.xhtml' },
    { name: 'Compras', path: BASE_URL + '/pages/purchases.xhtml' },
    { name: 'Ventas', path: BASE_URL + '/pages/sales.xhtml' },
    { name: 'Finanzas', path: BASE_URL + '/pages/finances.xhtml' },
    { name: 'Recursos Humanos', path: BASE_URL + '/pages/human_resources.xhtml', }
  ],
  RRHH: [
    { name: 'Inicio', path: BASE_URL + '/pages/home.xhtml' },
    { name: 'Analíticas', path: BASE_URL + '/pages/analytics.xhtml' },
    { name: 'Recursos Humanos', path: BASE_URL + '/pages/human_resources.xhtml', }
  ],
  Ventas: [
    { name: 'Inicio', path: BASE_URL + '/pages/home.xhtml' },
    { name: 'Analíticas', path: BASE_URL + '/pages/analytics.xhtml' },
    { name: 'Ventas', path: BASE_URL + '/pages/sales.xhtml' }
  ],
  Inventarista: [
    { name: 'Inicio', path: BASE_URL + '/pages/home.xhtml' },
    { name: 'Analíticas', path: BASE_URL + '/pages/analytics.xhtml' },
    { name: 'Inventario', path: BASE_URL + '/pages/inventory.xhtml' }
  ],
  Compras: [
    { name: 'Inicio', path: BASE_URL + '/pages/home.xhtml' },
    { name: 'Analíticas', path: BASE_URL + '/pages/analytics.xhtml' },
    { name: 'Compras', path: BASE_URL + '/pages/purchases.xhtml' }
  ],
  Finanzas: [
    { name: 'Inicio', path: BASE_URL + '/pages/home.xhtml' },
    { name: 'Analíticas', path: BASE_URL + '/pages/analytics.xhtml' },
    { name: 'Finanzas', path: BASE_URL + '/pages/finances.xhtml' }
  ],
  default: [{ name: 'Inicio', path: BASE_URL + '/pages/home.xhtml' }]
};

/**
 * Renderiza dinámicamente los enlaces del menú según el rol del usuario.
 * Utiliza el token JWT almacenado en sessionStorage para determinar el rol.
 * Si no hay token válido o el rol no tiene permisos definidos, se muestra solo la pestaña de Inicio.
 *
 * @throws {MenuRenderError} Si ocurre un error durante el renderizado del menú.
 */
export function renderMenuByRole() {
  try {
    const nav = document.getElementById('nav-links');
    if (!nav) throw new MenuRenderError('Elemento de navegación no encontrado');

    const token = sessionStorage.getItem('authToken');
    let data = null;

    try {
      data = decodeJWT(token);
    } catch (error) {
      data = null;
    }

    const role = data && data.role && menuItems[data.role] ? data.role : 'default';
    const items = menuItems[role];

    nav.innerHTML = '';

    items.forEach((item) => {
      const a = document.createElement('a');
      a.href = item.path;
      a.textContent = item.name;
      a.className = 'px-4 py-2 rounded-xl hover:bg-white hover:text-blue-800 transition';
      nav.appendChild(a);
    });
  } catch (e) {
    if (e instanceof MenuRenderError) throw e;
    throw new MenuRenderError(`Error al renderizar el menú: ${e.message}`);
  }
}

document.addEventListener('DOMContentLoaded', renderMenuByRole);
