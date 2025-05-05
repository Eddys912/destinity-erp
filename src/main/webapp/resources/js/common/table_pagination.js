/**
 * @file table_pagination.js
 * @description Componente reutilizable para gestionar la paginación visual de tablas con indicadores, botones y estilo activo.
 */

/**
 * @callback PageChangeCallback
 * @param {number} newPage - Página a cargar
 */

/**
 * Renderiza controles de paginación con navegación y resumen.
 *
 * @param {string} containerId - ID del contenedor HTML.
 * @param {number} totalItems - Total de elementos disponibles.
 * @param {number} itemsPerPage - Elementos por página.
 * @param {number} currentPage - Página activa actual (1-indexado).
 * @param {PageChangeCallback} onPageChange - Función callback que se ejecuta al cambiar de página.
 */
export function setupPagination(
  containerId,
  totalItems,
  itemsPerPage,
  currentPage,
  onPageChange
) {
  const totalPages = Math.ceil(totalItems / itemsPerPage);
  const container = document.getElementById(containerId);
  container.innerHTML = '';

  const wrapper = document.createElement('div');
  wrapper.className = 'flex justify-between items-center mt-6 px-4 w-full';

  const from = (currentPage - 1) * itemsPerPage + 1;
  const to = Math.min(currentPage * itemsPerPage, totalItems);
  const summary = document.createElement('div');
  summary.className = 'text-sm text-gray-500';
  summary.innerHTML = `Mostrando <span class="font-medium">${from}-${to}</span> de <span class="font-medium">${totalItems}</span> registros`;

  const controls = document.createElement('div');
  controls.className = 'flex gap-2';

  const prev = document.createElement('button');
  prev.className =
    'px-3 py-1 rounded border text-gray-500 hover:bg-gray-50 disabled:opacity-50';
  prev.innerHTML = `<i class="ph ph-caret-left"></i>`;
  prev.disabled = currentPage <= 1;
  prev.onclick = () => {
    if (currentPage > 1) onPageChange(currentPage - 1);
  };
  controls.appendChild(prev);

  const maxButtons = 3;
  let startPage = Math.max(currentPage - 1, 1);
  let endPage = Math.min(startPage + maxButtons - 1, totalPages);
  if (endPage - startPage < maxButtons - 1) {
    startPage = Math.max(endPage - maxButtons + 1, 1);
  }

  if (startPage > 1) {
    controls.appendChild(renderPageBtn(1, currentPage, onPageChange));
    if (startPage > 2) controls.appendChild(renderDots());
  }

  for (let i = startPage; i <= endPage; i++) {
    controls.appendChild(renderPageBtn(i, currentPage, onPageChange));
  }

  if (endPage < totalPages) {
    if (endPage < totalPages - 1) controls.appendChild(renderDots());
    controls.appendChild(renderPageBtn(totalPages, currentPage, onPageChange));
  }

  const next = document.createElement('button');
  next.className =
    'px-3 py-1 rounded border text-gray-500 hover:bg-gray-50 disabled:opacity-50';
  next.innerHTML = `<i class="ph ph-caret-right"></i>`;
  next.disabled = currentPage >= totalPages;
  next.onclick = () => {
    if (currentPage < totalPages) onPageChange(currentPage + 1);
  };
  controls.appendChild(next);

  wrapper.appendChild(summary);
  wrapper.appendChild(controls);
  container.appendChild(wrapper);
}

/**
 * Crea un botón de página individual.
 *
 * @param {number} page - Número de página representado por el botón.
 * @param {number} activePage - Página activa actualmente.
 * @param {PageChangeCallback} onClickFn - Función a ejecutar al hacer clic en el botón.
 * @returns {HTMLButtonElement}
 */
function renderPageBtn(page, activePage, onClickFn) {
  const btn = document.createElement('button');
  btn.innerText = page;
  btn.className =
    page === activePage
      ? 'px-3 py-1 rounded border bg-purple-600 text-white'
      : 'px-3 py-1 rounded border text-gray-700 hover:bg-gray-50';
  btn.onclick = () => onClickFn(page);
  return btn;
}

/**
 * Crea un separador visual (...) entre botones de página.
 *
 * @returns {HTMLButtonElement}
 */
function renderDots() {
  const btn = document.createElement('button');
  btn.disabled = true;
  btn.innerHTML = `<i class="ph ph-dots-three-outline"></i>`;
  btn.className = 'px-3 py-1 rounded border text-gray-700';
  return btn;
}
