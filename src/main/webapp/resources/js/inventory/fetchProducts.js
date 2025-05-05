/**
 * @file fetchProducts.js
 * @description Controlador de productos que carga todos los productos desde una API, renderiza la tabla y aplica paginación.
 *
 * Este archivo depende de los módulos:
 * - renderTable (para renderizar filas de empleados en la tabla)
 * - setupPagination (para mostrar controles de paginación)
 */
import { renderTable } from '../common/table_render.js';
import { setupPagination } from '../common/table_pagination.js';

document.addEventListener('DOMContentLoaded', async function () {
  const BASE_API = window.location.origin + '/destinity-erp';
  let currentPage = 1;
  const itemsPerPage = 5;
  let allProducts = [];

  const columns = [
    {
      content: (u) =>
        `
        <div class="flex items-center gap-3">
            <div class="bg-blue-100 p-2 rounded">
                <i class="ph ph-bed text-blue-600"></i>
            </div>
            <span class="font-medium">${u.name}</span>
        </div>` ?? '-',
    },
    {
      content: (u) =>
        `
        <span class="font-medium">${u.stock}</span>
        <div class="w-32 h-2 bg-gray-200 rounded-full mt-1">
            <div class="w-2/3 h-2 bg-green-500 rounded-full"></div>
        </div>` ?? '-',
    },
    {
      content: (u) =>
        `
        <span class="bg-blue-100 text-blue-800 px-3 py-1 rounded-full text-sm">${u.category}</span>` ??
        '-',
    },
    {
      content: (u) =>
        `
        <span class="bg-green-100 text-green-800 px-3 py-1 rounded-full text-sm font-medium flex items-center gap-1">
            <i class="ph ph-check-circle"></i> ${u.status}
        </span>` ?? '-',
    },
  ];
  const actions = [
    {
      icon: 'ph-eye',
      color: 'text-blue-600 bg-blue-100',
      title: 'Ver perfil',
      onClick: 'viewProduct',
    },
    {
      icon: 'ph-pencil',
      color: 'text-yellow-600 bg-yellow-100',
      title: 'Editar',
      onClick: 'editProduct',
    },
    {
      icon: 'ph-trash',
      color: 'text-red-600 bg-red-100',
      title: 'Eliminar',
      onClick: 'deleteProduct',
    },
  ];

  async function fetchAllProducts() {
    try {
      const response = await fetch(`${BASE_API}/api/products/all`);
      allProducts = await response.json();
    } catch (error) {
      console.error('Error al obtener productos:', error);
    }
  }

  function loadPage(page) {
    currentPage = page;
    const start = (page - 1) * itemsPerPage;
    const pagedData = allProducts.slice(start, start + itemsPerPage);

    renderTable('tbodyInventory', pagedData, columns, actions);
    setupPagination(
      'paginationInventory',
      allProducts.length,
      itemsPerPage,
      currentPage,
      loadPage
    );
  }
  await fetchAllProducts();
  loadPage(1);
});
