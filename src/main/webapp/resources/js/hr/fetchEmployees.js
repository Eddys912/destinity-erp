/**
 * @file fetchEmployees.js
 * @description Controlador de empleados que carga todos los empleados desde una API, renderiza la tabla y aplica paginación.
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
  let allUsers = [];

  const columns = [
    {
      className: 'p-4 font-medium text-purple-600',
      content: (_, i) => `EMP-2025-${String(i + 1).padStart(3, '0')}`,
    },
    {
      className: 'p-4 flex items-center gap-2',
      content: (u) => {
        const initials = `${u.firstName[0] ?? ''}${u.lastName[0] ?? ''}`.toUpperCase();
        return (
          `
          <div class="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center">
              <span class="font-bold text-blue-800">${initials}</span>
          </div>
          ${u.firstName} ${u.lastName}` ?? '-'
        );
      },
    },
    { content: (u) => u.department ?? '-' },
    { content: (u) => u.role ?? '-' },
    { content: (u) => formatDate(u.createdAt) ?? '-' },
    {
      content: (u) => `
        <span class="bg-green-100 text-green-800 px-3 py-1 rounded-full text-sm font-medium flex items-center gap-1 w-fit">
          <i class="ph ph-check-circle"></i> ${u.status}
        </span>` ?? '-',
    },
  ];
  const actions = [
    {
      icon: 'ph-eye',
      color: 'text-blue-600 bg-blue-100',
      title: 'Ver perfil',
      onClick: 'viewUser',
    },
    {
      icon: 'ph-pencil',
      color: 'text-yellow-600 bg-yellow-100',
      title: 'Editar',
      onClick: 'editUser',
    },
    {
      icon: 'ph-trash',
      color: 'text-red-600 bg-red-100',
      title: 'Eliminar',
      onClick: 'deleteUser',
    },
  ];

  async function fetchAllUsers() {
    try {
      const response = await fetch(`${BASE_API}/api/users/all`);
      allUsers = await response.json();
    } catch (error) {
      console.error('Error al obtener empleados:', error);
    }
  }

  function loadPage(page) {
    currentPage = page;
    const start = (page - 1) * itemsPerPage;
    const pagedData = allUsers.slice(start, start + itemsPerPage);

    renderTable('tbodyRh', pagedData, columns, actions);
    setupPagination(
      'paginationRh',
      allUsers.length,
      itemsPerPage,
      currentPage,
      loadPage
    );
  }

  await fetchAllUsers();
  loadPage(1);
});
