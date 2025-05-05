/**
 * @file fetchLogin.js
 * @description Controlador del login para la autenticación del sistema.
 * Este archivo gestiona el proceso de inicio de sesión, incluyendo:
 * - Validación de campos del formulario
 * - Comunicación con el API de autenticación
 * - Manejo de respuestas y errores
 * - Almacenamiento del token de sesión
 * - Redirección tras inicio de sesión exitoso
 */
import {
  showFieldError,
  clearErrors,
  showGeneralError,
  validateForm,
} from './common/form_helpers.js';

document.addEventListener('DOMContentLoaded', function () {
  const API_BASE = window.location.origin + '/destinity-erp';

  const loginForm = document.getElementById('login-form');
  const emailInput = document.getElementById('email');
  const passwordInput = document.getElementById('password');
  const emailError = document.getElementById('email-error');
  const passwordError = document.getElementById('password-error');
  const generalError = document.getElementById('general-error');
  const loginButton = document.getElementById('login-button');
  const spinner = document.getElementById('spinner');

  loginForm.addEventListener('submit', function (event) {
    event.preventDefault();

    const isValid = validateForm([
      {
        field: emailInput,
        errorElement: emailError,
        message: 'El campo correo electrónico no puede estar vacío',
      },
      {
        field: passwordInput,
        errorElement: passwordError,
        message: 'El campo contraseña no puede estar vacío',
      },
    ]);

    if (!isValid) return;

    setLoadingState(true);

    fetch(`${API_BASE}/api/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        email: emailInput.value.trim(),
        password: passwordInput.value.trim(),
      }),
    })
      .then(async (response) => {
        const data = await response.json();
        if (!response.ok)
          throw new Error(data.message || 'Error al iniciar sesión');
        return data;
      })
      .then((loginData) => {
        const token = loginData.token;
        if (!token)
          showGeneralError(generalError, 'No se pudo completar el inicio de sesión.');
        sessionStorage.setItem('authToken', token);
        window.location.href = `${API_BASE}/pages/home.xhtml`;
      })
      .catch((error) => {
        showGeneralError(generalError, error.message);
      })
      .finally(() => {
        setLoadingState(false);
      });
  });

  /**
   * Cambia el estado visual del botón durante la carga
   * @param {boolean} isLoading - Indica si está en proceso de carga
   */
  function setLoadingState(isLoading) {
    loginButton.disabled = isLoading;
    loginButton.querySelector('span').textContent = isLoading
      ? 'Procesando...'
      : 'Ingresar';
    if (isLoading) {
      spinner.classList.remove('hidden');
    } else {
      spinner.classList.add('hidden');
    }
  }

  // Limpiar errores al escribir
  [emailInput, passwordInput].forEach((input) => {
    input.addEventListener('input', function () {
      this.classList.remove('input-error');
      document.getElementById(`${this.id}-error`).classList.add('hidden');
      generalError.classList.add('hidden');
    });
  });
});
