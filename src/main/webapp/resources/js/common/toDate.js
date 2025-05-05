/**
 * @file toDate.js
 * @description Función utilitaria para convertir un timestamp a formato legible dd/mm/yyyy.
 */

/**
 * Convierte un valor timestamp a formato de fecha dd/mm/yyyy.
 *
 * @param {string|number|Date} timestamp - Fecha en formato timestamp o ISO string.
 * @returns {string} Fecha en formato "dd/mm/yyyy", o "-" si el valor no es válido.
 */
export function formatDate(timestamp) {
  if (!timestamp) return '-';
  const date = new Date(timestamp);
  const day = String(date.getDate()).padStart(2, '0');
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const year = date.getFullYear();
  return `${day}/${month}/${year}`;
}
