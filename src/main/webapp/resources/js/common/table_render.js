/**
 * @file table_render.js
 * @description Componente genérico para renderizar tablas dinámicas en el DOM a partir de datos personalizados.
 */

/**
 * @typedef {Object} TableColumn
 * @property {string} [className] - Clases CSS que se aplicarán al <td>.
 * @property {function(Object, number): string} content - Función que recibe un objeto y su índice, y devuelve contenido HTML.
 */

/**
 * @typedef {Object} TableAction
 * @property {string} icon - Nombre de la clase del ícono (ej. "ph-eye").
 * @property {string} color - Clases de color Tailwind aplicadas al botón (ej. "text-blue-600 bg-blue-100").
 * @property {string} title - Texto que aparece al pasar el cursor sobre el botón.
 * @property {string} onClick - Nombre de la función global a ejecutar al hacer clic (se pasa el ID del item como parámetro).
 */

/**
 * Renderiza una tabla HTML con contenido personalizado y botones de acción opcionales.
 *
 * @param {string} tbodyId - ID del elemento <tbody> donde se colocarán las filas.
 * @param {Array<Object>} data - Lista de objetos a renderizar como filas.
 * @param {Array<TableColumn>} columns - Definición de las columnas a renderizar.
 * @param {Array<TableAction>} [actions=[]] - Acciones que se renderizan en la última columna.
 */
export function renderTable(tbodyId, data, columns, actions = []) {
  const tbody = document.getElementById(tbodyId);
  tbody.innerHTML = '';

  data.forEach((item, index) => {
    const row = document.createElement('tr');
    row.className = 'border-b hover:bg-purple-50 transition duration-150';

    let html = columns
      .map((col) => {
        const value = col.content(item, index);
        const cls = col.className || 'p-4';
        return `<td class="${cls}">${value}</td>`;
      })
      .join('');

    if (actions.length > 0) {
      html += `<td class="p-4"><div class="flex justify-center gap-2">`;
      actions.forEach(({ icon, color, title, onClick }) => {
        html += `
          <button class="${color} p-2 rounded transition duration-150" title="${title}" onclick="${onClick}(${item.id})">
            <i class="ph ${icon}"></i>
          </button>`;
      });
      html += `</div></td>`;
    }

    row.innerHTML = html;
    tbody.appendChild(row);
  });
}
