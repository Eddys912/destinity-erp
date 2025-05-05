/**
 * @file form_helpers.js
 * @description Utilidades reutilizables para validación de formularios y manejo de errores visuales.
 */

/**
 * Error personalizado para validación de formularios
 */
export class FormValidationError extends Error {
  constructor(message) {
    super(message);
    this.name = 'FormValidationError';
  }
}

/**
 * Muestra un mensaje de error en un campo de entrada específico.
 *
 * @param {HTMLElement} field - Campo de formulario al que aplicar el estilo de error.
 * @param {HTMLElement} errorElement - Elemento donde se muestra el mensaje de error.
 * @param {string} message - Mensaje de error a mostrar.
 * @throws {FormValidationError} Si alguno de los parámetros es inválido.
 */
export function showFieldError(field, errorElement, message) {
  if (!field || !(field instanceof HTMLElement))
    throw new FormValidationError('Campo de formulario inválido');

  if (!errorElement || !(errorElement instanceof HTMLElement))
    throw new FormValidationError('Elemento de error inválido');

  field.classList.add('input-error');
  errorElement.textContent = message;
  errorElement.classList.remove('hidden');
  field.focus();
}

/**
 * Oculta todos los mensajes de error visibles y resetea los estilos de los campos.
 *
 * @param {Array<HTMLElement>} fields - Arreglo de campos de formulario.
 * @param {Array<HTMLElement>} errorElements - Arreglo de elementos de error.
 * @param {HTMLElement|null} generalError - Contenedor de error general, opcional.
 * @throws {FormValidationError} Si los parámetros no son arreglos válidos.
 */
export function clearErrors(fields, errorElements, generalError = null) {
  if (!Array.isArray(fields))
    throw new FormValidationError('Se esperaba un arreglo de campos');

  if (!Array.isArray(errorElements))
    throw new FormValidationError('Se esperaba un arreglo de elementos de error');

  fields.forEach((input) => input.classList.remove('input-error'));
  errorElements.forEach((error) => {
    error.classList.add('hidden');
    error.textContent = '';
  });

  if (generalError) {
    generalError.classList.add('hidden');
    const p = generalError.querySelector('p');
    if (p) p.textContent = '';
  }
}

/**
 * Muestra un mensaje de error general en la interfaz.
 *
 * @param {HTMLElement} generalError - Contenedor HTML donde se mostrará el mensaje.
 * @param {string} message - Texto del mensaje general.
 * @throws {FormValidationError} Si el contenedor de error no es válido.
 */
export function showGeneralError(generalError, message) {
  if (!generalError || !(generalError instanceof HTMLElement)) {
    throw new FormValidationError('Contenedor de error general inválido');
  }

  const p = generalError.querySelector('p');
  if (p) p.textContent = message;

  generalError.classList.remove('hidden', 'opacity-0');
  generalError.classList.add('shake');

  setTimeout(() => generalError.classList.remove('shake'), 500);
}

/**
 * Valida si los campos del formulario contienen valores y muestra errores si están vacíos.
 *
 * @param {Array<Object>} fields - Arreglo de objetos con { field, errorElement, message }
 * @returns {boolean} true si todos los campos son válidos, false si alguno está vacío.
 * @throws {FormValidationError} Si el parámetro fields no es un arreglo válido.
 */
export function validateForm(fields) {
  if (!Array.isArray(fields))
    throw new FormValidationError('Se esperaba un arreglo de campos para validar');

  const errors = [];
  fields.forEach(({ field, errorElement, message }) => {
    if (!field.value.trim()) {
      showFieldError(field, errorElement, message);
      errors.push(message);
    }
  });
  return errors.length === 0;
}
