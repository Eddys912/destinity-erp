/**
 * @file fetchSales.js
 * @description Controlador de ventas que carga todas las ventas desde una API, renderiza la tabla y aplica paginación.
 *
 * Este archivo depende de los módulos:
 * - renderTable (para renderizar filas de empleados en la tabla)
 * - setupPagination (para mostrar controles de paginación)
 * - formatDate (para formatear timestamps)
 */
import { renderTable } from '../common/table_render.js';
import { setupPagination } from '../common/table_pagination.js';
import { formatDate } from '../common/toDate.js';

document.addEventListener('DOMContentLoaded', async function () {
  const BASE_API = window.location.origin + '/destinity-erp';
  let currentPage = 1;
  const itemsPerPage = 5;
  let allSales = [];

  const columns = [
    {
      className: 'p-4 font-medium text-purple-600',
      content: (_, i) => `VTA-2025-${String(i + 1).padStart(3, '0')}`,
    },
    { content: (u) => u.name ?? '-' },
    { content: (u) => formatDate(u.sale) ?? '-' },
    { className: 'p-4 font-medium', content: (u) => `$${u.total}` ?? '-' },
    {
      content: (u) =>
        `<span class="flex items-center gap-1">
            <i class="ph ph-credit-card text-gray-600"></i> ${u.payment}
        </span>` ?? '-',
    },
    {
      content: (u) =>
        `
        <span class="bg-green-100 text-green-800 px-3 py-1 rounded-full text-sm font-medium flex items-center gap-1 w-fit">
            <i class="ph ph-check-circle"></i> ${u.status}
        </span>` ?? '-',
    },
  ];
  const actions = [
    {
      icon: 'ph-eye',
      color: 'text-blue-600 bg-blue-100',
      title: 'Ver venta',
      onClick: 'viewSale',
    },
    {
      icon: 'ph-trash',
      color: 'text-red-600 bg-red-100',
      title: 'Eliminar',
      onClick: 'deleteSale',
    },
  ];

  async function fetchAllSales() {
    try {
      const response = await fetch(`${BASE_API}/api/sales/all`);
      allSales = await response.json();
    } catch (error) {
      console.error('Error al obtener ventas:', error);
    }
  }

  function loadPage(page) {
    currentPage = page;
    const start = (page - 1) * itemsPerPage;
    const pagedData = allSales.slice(start, start + itemsPerPage);

    renderTable('tbodySales', pagedData, columns, actions);
    setupPagination(
      'paginationSales',
      allSales.length,
      itemsPerPage,
      currentPage,
      loadPage
    );
  }

  await fetchAllSales();
  loadPage(1);
});
