package com.destinity.erp.hr;

import com.destinity.erp.utils.CustomException;
import com.destinity.erp.utils.InputValidator;
import com.destinity.erp.utils.PasswordHasher;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.bson.types.ObjectId;

/**
 * Servicio encargado de manejar la lógica de negocio de los usuarios.
 * Procesa las operaciones antes de ser delegadas al repositorio y
 * prepara los datos para el controlador.
 */
@ApplicationScoped
public class UserService {

    private static final Logger LOGGER = Logger.getLogger(UserService.class.getName());
    private static final Pattern EMAIL_PATTERN
            = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String DEFAULT_USER_TYPE = "employee";

    @Inject
    private UserRepository userRepository;

    /**
     * Crea un nuevo empleado
     *
     * @param employee modelo de usuario a crear
     * @return DTO del usuario creado o null si falla
     */
    public UserDTO createEmployee(UserModel employee) {
        if (employee.getId() == null) employee.setId(new ObjectId());
        if (employee.getStatus() == null || employee.getStatus().isBlank()) employee.setStatus("Activo");
        if (employee.getCreatedAt() == null) employee.setCreatedAt(LocalDateTime.now());
        if (employee.getUserType() == null || employee.getUserType().isBlank()) employee.setUserType("employee");
        
        InputValidator.isNotEmpty(employee.getEmployeeData().getRole(), "Rol");
        InputValidator.isNotEmpty(employee.getEmployeeData().getDepartment(), "Departamento");
        isValidUser(employee);

        if (employee.getEmployeeData().getSalary() == null || employee.getEmployeeData().getSalary() <= 0.0)
            throw CustomException.business("El salario debe ser mayor a 0");
        
        String hashedPassword = PasswordHasher.hashPassword(employee.getPassword());
        employee.setPassword(hashedPassword);
        
        String userId = userRepository.saveUser(employee);
        if (userId == null) {
            LOGGER.log(Level.WARNING, "No se pudo guardar el empleado: {0}", employee.getId());
            return null;
        }

        Optional<UserModel> createdUser = userRepository.findUserById(userId);
        createdUser.ifPresent(p -> LOGGER.log(Level.INFO, "Empleado creado: {0}", p.getId()));
        return createdUser.map(this::convertToDTO).orElse(null);
    }

    /**
     * Crea un nuevo proveedor
     *
     * @param provider modelo de usuario a crear
     * @return DTO del usuario creado o null si falla
     */
    public UserDTO createProvider(UserModel provider) {
        if (provider.getId() == null) provider.setId(new ObjectId());
        if (provider.getStatus() == null || provider.getStatus().isBlank()) provider.setStatus("Activo");
        if (provider.getCreatedAt() == null) provider.setCreatedAt(LocalDateTime.now());
        if (provider.getUserType() == null || provider.getUserType().isBlank()) provider.setUserType("provider");

        isValidUser(provider);
        InputValidator.isNotEmpty(provider.getPassword(), "Contraseña");
        InputValidator.isNotEmpty(provider.getProviderData().getCompany(), "Compañia");
        InputValidator.isNotEmpty(provider.getProviderData().getServiceType(), "Tipo servicio");
        InputValidator.isNotEmpty(provider.getProviderData().getPhone(), "Telefono");
        if (provider.getProviderData().getPhone().length() != 10)
            throw CustomException.business("El teléfono debe contener 10 digitos");

        String userId = userRepository.saveUser(provider);
        if (userId == null) {
            LOGGER.log(Level.WARNING, "No se pudo guardar el proveedor: {0}", provider.getId());
            return null;
        }

        Optional<UserModel> createdUser = userRepository.findUserById(userId);
        createdUser.ifPresent(p -> LOGGER.log(Level.INFO, "Proveedor creado: {0}", p.getId()));
        return createdUser.map(this::convertToDTO).orElse(null);
    }

    /**
     * Obtiene todos los usuarios paginados
     *
     * @param page número de página (desde 0)
     * @param pageSize tamaño de página
     * @param userType tipo de usuario
     * @return lista de DTOs de usuarios
     */
    public List<UserDTO> getAllUsers(int page, int pageSize, String userType) {
        if (page < 0) page = DEFAULT_PAGE;
        if (pageSize <= 0) pageSize = DEFAULT_PAGE_SIZE;
        if (userType == null) userType = DEFAULT_USER_TYPE;

        List<UserModel> users = userRepository.findAllUsers(page, pageSize, userType);
        if (users == null || users.isEmpty()) {
            LOGGER.warning("No hay usuarios registrados");
            throw CustomException.notFound("No hay usuarios registrados");
        }
        LOGGER.log(Level.INFO, "Usuarios obtenidos: {0}", users.size());
        return users.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Obtiene un usuario por su ID
     *
     * @param id identificador del usuario
     * @return DTO del usuario o null si no existe
     */
    public UserDTO getUserById(String id) {
        Optional<UserModel> user = userRepository.findUserById(id);
        if (user.isEmpty()) {
            LOGGER.log(Level.WARNING, "No se encontró el usuario con ID: {0}", id);
            throw CustomException.notFound("No existe el usuario con el identificador proporcionado");
        }
        LOGGER.log(Level.INFO, "Venta encontrado: {0}", user.get().getId());
        return convertToDTO(user.get());
    }

    /**
     * Obtiene un usuario por su correo
     *
     * @param email correo del usuario
     * @return DTO del usuario o null si no existe
     */
    public UserDTO getUserByEmail(String email) {
        Optional<UserModel> user = userRepository.findUserByEmail(email);
        if (user.isEmpty()) {
            LOGGER.log(Level.WARNING, "No se encontró el usuario con correo: {0}", email);
            throw CustomException.notFound("No existe la usuario con el correo proporcionado");
        }
        LOGGER.log(Level.INFO, "Usuario encontrado: {0}", user.get().getId());
        return convertToDTO(user.get());
    }

    /**
     * Obtiene usuarios por estatus
     *
     * @param status estatus a buscar
     * @param userType tipo de usuario a buscar
     * @return lista de DTOs de usuarios
     */
    public List<UserDTO> getUsersByStatus(String status, String userType) {
        if (userType == null) userType = DEFAULT_USER_TYPE;

        List<UserModel> users = userRepository.findUserByStatus(status, userType);
        if (users == null || users.isEmpty()) {
            LOGGER.log(Level.WARNING, "No hay usuarios con el estatus: {0}", status);
            throw CustomException.notFound("No hay usuarios con el estatus" + status);
        }
        LOGGER.log(Level.INFO, "Usuarios encontrados con el estatus: {0}, cantidad: {1}",
                new Object[]{status, users.size()});
        return users.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Obtiene empleados por departamento
     *
     * @param department departamento a buscar
     * @return lista de DTOs de usuarios
     */
    public List<UserDTO> getEmployeeByDepartment(String department) {
        List<UserModel> users = userRepository.findEmployeeByDepartment(department);
        if (users == null || users.isEmpty()) {
            LOGGER.log(Level.WARNING, "No hay empleados en el departamento: {0}", department);
            throw CustomException.notFound("No hay empleados en el departamento" + department);
        }
        LOGGER.log(Level.INFO, "Empleados encontrados en el departamento: {0}, cantidad: {1}",
                new Object[]{department, users.size()});
        return users.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Obtiene provedores por servicio
     *
     * @param service servicio a buscar
     * @return lista de DTOs de usuarios
     */
    public List<UserDTO> getProvidersByService(String service) {
        List<UserModel> users = userRepository.findEmployeeByDepartment(service);
        if (users == null || users.isEmpty()) {
            LOGGER.log(Level.WARNING, "No hay proveedores del servicio: {0}", service);
            throw CustomException.notFound("No hay proveedores del servicio" + service);
        }
        LOGGER.log(Level.INFO, "Proveedores encontrados del servicio: {0}, cantidad: {1}",
                new Object[]{service, users.size()});
        return users.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Busqueda general por texto (coincidencia parcial)
     *
     * @param textSearch texto a buscar
     * @return lista de DTOs de usuarios
     */
    public List<UserDTO> searchEmployeesByName(String textSearch) {
        List<UserModel> users = userRepository.findUsersByText(textSearch, "employee");
        if (users == null || users.isEmpty()) {
            LOGGER.log(Level.WARNING, "No se encontraron empleados: {0}", textSearch);
            throw CustomException.notFound("No se encontraron empleados con el texto proporcionado");
        }
        LOGGER.log(Level.INFO, "Empleados encontrados con el texto: {0}, cantidad: {1}",
                new Object[]{textSearch, users.size()});
        return users.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Actualiza un usuario existente
     *
     * @param id identificador de la usuario
     * @param user datos actualizados
     * @throws CustomException si la usuario no es válida
     * @return DTO del usuario actualizado o null si falla
     */
    public UserDTO updateUser(String id, UserModel user) {
        Optional<UserModel> optionalExistingUser = userRepository.findUserById(id);
        if (optionalExistingUser.isEmpty()) {
            LOGGER.log(Level.WARNING, "No se encontró el usuario para actualizar con ID: {0}", id);
            throw CustomException.notFound("No existe el usuario con el identificador proporcionado");
        }

        UserModel existingUser = optionalExistingUser.get();

        if (isSameUser(existingUser, user)) {
            LOGGER.log(Level.WARNING, "No se detectaron cambios al actualizar el usuario con ID: {0}", id);
            throw CustomException.business("No se detectaron cambios. El usuario no fue modificado");
        }

        isValidUser(user);
        applyChanges(existingUser, user);
        existingUser.setUpdatedAt(LocalDateTime.now());

        String updatedId = userRepository.updateUser(existingUser);
        if (updatedId == null) {
            LOGGER.log(Level.WARNING, "No se pudo actualizar el usuario con ID: {0}", id);
            return null;
        }

        Optional<UserModel> updatedUser = userRepository.findUserById(updatedId);
        updatedUser.ifPresent(p -> LOGGER.log(Level.INFO, "Usuario actualizado: {0}", p.getId()));
        return updatedUser.map(this::convertToDTO).orElse(null);
    }

    /**
     * Elimina un usuario
     *
     * @param id identificador del usuario
     * @throws CustomException si el usuario no es válido
     * @return true si se eliminó correctamente
     */
    public boolean deleteUser(String id) {
        Optional<UserModel> existingUser = userRepository.findUserById(id);
        if (existingUser.isEmpty()) {
            LOGGER.log(Level.WARNING, "No se encontró el usuario para eliminar con ID: {0}", id);
            throw CustomException.notFound("No existe el usuario con el identificador proporcionado");
        }

        boolean deleted = userRepository.deleteUser(id);
        if (!deleted) 
            LOGGER.log(Level.WARNING, "Error al intentar eliminar la usuario con ID: {0}", id);
        LOGGER.log(Level.INFO, "Usuario eliminada con ID: {0}", id);
        return deleted;
    }

    /**
     * Obtiene el conteo total de usuarios
     *
     * @return número total de usuarios
     */
    public long getTotalUserCount() {
        long count = userRepository.countUsers();
        LOGGER.log(Level.INFO, "Total de usuarios registrados: {0}", count);
        return count;
    }

    /**
     * Valida un usuario
     *
     * @param user usuario a validar
     * @throws CustomException si el usuario no es válido
     * @throws InputValidator si algún campo no es válido
     */
    private void isValidUser(UserModel user) {
        if (user == null) 
            throw CustomException.business("El usuario no puede estar vacio");

        InputValidator.isNotEmpty(user.getFirstName(), "Nombre");
        InputValidator.isNotEmpty(user.getLastName(), "Apellido paterno");
        InputValidator.isNotEmpty(user.getEmail(), "Correo");
        InputValidator.isNotEmpty(user.getPassword(), "Contraseña");

        if (user.getPassword().length() < 8)
            throw CustomException.business("La contraseña debe contener mínimo 8 carácteres.");

        if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) 
            throw CustomException.business("El correo electrónico no tiene un formato válido.");

        boolean hasEmployee = user.getEmployeeData() != null;
        boolean hasProvider = user.getProviderData() != null;

        if (hasEmployee && hasProvider) {
            throw CustomException.business("El usuario no puede ser empleado y proveedor al mismo tiempo.");
        }

        if (!hasEmployee && !hasProvider) {
            throw CustomException.business("El usuario debe ser un empleado o un proveedor.");
        }

        if (hasEmployee) {
            UserModel.EmployeeData emp = user.getEmployeeData();
            InputValidator.isNotEmpty(emp.getRole(), "Rol");
            InputValidator.isNotEmpty(emp.getDepartment(), "Departamento");
            if (emp.getSalary() == null || emp.getSalary() <= 0) {
                throw CustomException.business("El salario del empleado debe ser mayor a 0.");
            }
        }

        if (hasProvider) {
            UserModel.ProviderData prov = user.getProviderData();
            InputValidator.isNotEmpty(prov.getCompany(), "Empresa");
            InputValidator.isNotEmpty(prov.getServiceType(), "Tipo de Servicio");
            InputValidator.isNotEmpty(prov.getPhone(), "Teléfono de Contacto");
        }
    }

    /**
     * Comprueba si hay cambios en el usuario
     *
     * @param target nuevos datos del usuario
     * @param source datos anteriores del usuario
     * @return true si son iguales
     */
    private boolean isSameUser(UserModel target, UserModel source) {
        if (!Objects.equals(source.getFirstName(), target.getFirstName())) return false;
        if (!Objects.equals(source.getLastName(), target.getLastName())) return false;
        if (!Objects.equals(source.getMiddleName(), target.getMiddleName())) return false;
        if (!Objects.equals(source.getEmail(), target.getEmail())) return false;
        if (!Objects.equals(source.getStatus(), target.getStatus())) return false;

        boolean sourceHasEmployee = source.getEmployeeData() != null;
        boolean targetHasEmployee = target.getEmployeeData() != null;
        boolean sourceHasProvider = source.getProviderData() != null;
        boolean targetHasProvider = target.getProviderData() != null;

        if (sourceHasEmployee != targetHasEmployee || sourceHasProvider != targetHasProvider) {
            return false;
        }

        if (sourceHasEmployee) {
            UserModel.EmployeeData srcEmp = source.getEmployeeData();
            UserModel.EmployeeData tgtEmp = target.getEmployeeData();
            if (!Objects.equals(srcEmp.getRole(), tgtEmp.getRole())) return false;
            if (!Objects.equals(srcEmp.getDepartment(), tgtEmp.getDepartment())) return false;
            if (!Objects.equals(srcEmp.getSalary(), tgtEmp.getSalary())) return false;
        }

        if (sourceHasProvider) {
            UserModel.ProviderData srcProv = source.getProviderData();
            UserModel.ProviderData tgtProv = target.getProviderData();
            if (!Objects.equals(srcProv.getCompany(), tgtProv.getCompany())) return false;
            if (!Objects.equals(srcProv.getServiceType(), tgtProv.getServiceType())) return false;
            if (!Objects.equals(srcProv.getPhone(), tgtProv.getPhone())) return false;
        }

        return true;
    }

    /**
     * Aplica cambios de un usuario a otro
     *
     * @param target usuario destino
     * @param source usuario origen
     */
    private void applyChanges(UserModel target, UserModel source) {
        target.setFirstName(source.getFirstName());
        target.setLastName(source.getLastName());
        target.setMiddleName(source.getMiddleName());
        target.setEmail(source.getEmail());
        target.setStatus(source.getStatus());

        boolean sourceHasEmployee = source.getEmployeeData() != null;
        boolean targetHasEmployee = target.getEmployeeData() != null;
        boolean sourceHasProvider = source.getProviderData() != null;
        boolean targetHasProvider = target.getProviderData() != null;

        if (sourceHasEmployee && targetHasEmployee) {
            UserModel.EmployeeData srcEmp = source.getEmployeeData();
            UserModel.EmployeeData tgtEmp = target.getEmployeeData();

            tgtEmp.setRole(srcEmp.getRole());
            tgtEmp.setDepartment(srcEmp.getDepartment());
            tgtEmp.setSalary(srcEmp.getSalary());
        } else if (sourceHasProvider && targetHasProvider) {
            UserModel.ProviderData srcProv = source.getProviderData();
            UserModel.ProviderData tgtProv = target.getProviderData();

            tgtProv.setCompany(srcProv.getCompany());
            tgtProv.setServiceType(srcProv.getServiceType());
            tgtProv.setPhone(srcProv.getPhone());
        } else {
            throw CustomException.business("No se puede aplicar cambios entre tipos de usuario diferentes.");
        }
    }

    /**
     * Convierte un modelo a DTO
     *
     * @param user modelo de usuario
     * @return DTO de usuario
     */
    private UserDTO convertToDTO(UserModel user) {
        if (user == null) return null;
        return new UserDTO(user);
    }
}
