package com.destinity.erp.hr;

import com.destinity.erp.utils.CustomException;
import com.destinity.erp.utils.RestExceptionHandler;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Controlador REST encargado de las peticiones con los usuarios.
 * Expone los endpoint de la API para registrar, consultar y
 * gestionar usuarios a tráves del sistema.
 */
@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserController {

    private static final Logger LOGGER = Logger.getLogger(UserController.class.getName());

    @Inject
    private UserService userService;

    /**
     * Crea un nuevo empleado
     *
     * @param employee datos del empleado a crear
     * @return respuesta con el empleado creado o error
     */
    @POST
    @Path("/employees")
    public Response createEmployee(UserModel employee) {
        try {
            userService.createEmployee(employee);
            return Response.status(Response.Status.CREATED)
                    .entity(Map.of("message", "Empleado creado satisfactoriamente"))
                    .build();
        } catch (CustomException ex) {
            return RestExceptionHandler.handleCustomException(ex);
        } catch (Exception e) {
            return RestExceptionHandler.unexpectedCustomException(e);
        }
    }

    /**
     * Crea un proveedor usuario
     *
     * @param provider datos del proveedor a crear
     * @return respuesta con el proveedor creado o error
     */
    @POST
    @Path("/providers")
    public Response createProvider(UserModel provider) {
        try {
            userService.createProvider(provider);
            return Response.status(Response.Status.CREATED)
                    .entity(Map.of("message", "Proveedor creado satisfactoriamente"))
                    .build();
        } catch (CustomException ex) {
            return RestExceptionHandler.handleCustomException(ex);
        } catch (Exception e) {
            return RestExceptionHandler.unexpectedCustomException(e);
        }
    }

    /**
     * Obtiene usuarios paginados
     *
     * @param page número de página (desde 0)
     * @param size tamaño de página
     * @param type_user tipo de usuario
     * @return respuesta con la lista paginada de usuarios
     */
    @GET
    @Path("/all")
    public Response getAllUsers(
            @QueryParam("page") int page,
            @QueryParam("size") int size,
            @QueryParam("type_user") String type_user) {
        try {
            List<UserDTO> users = userService.getAllUsers(page, size, type_user);
            long totalCount = userService.getTotalUserCount();

            return Response.ok(users)
                    .header("X-Total-Count", totalCount)
                    .header("X-Page", page)
                    .header("X-Page-Size", size)
                    .build();
        } catch (CustomException ex) {
            return RestExceptionHandler.handleCustomException(ex);
        } catch (Exception e) {
            return RestExceptionHandler.unexpectedCustomException(e);
        }
    }

    /**
     * Obtiene un usuario por su ID
     *
     * @param id identificador del usuario
     * @return respuesta con el usuario o error si no existe
     */
    @GET
    public Response getUserById(@QueryParam("id") String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("El parámetro de búsqueda es requerido")
                        .build();
            }
            UserDTO user = userService.getUserById(id);
            return Response.ok(user).build();
        } catch (CustomException ex) {
            return RestExceptionHandler.handleCustomException(ex);
        } catch (Exception e) {
            return RestExceptionHandler.unexpectedCustomException(e);
        }
    }

    /**
     * Obtiene un usuario por su correo
     *
     * @param email correo del usuario
     * @return respuesta con el usuario o error si no existe
     */
    @GET
    @Path("/email")
    public Response getUserByEmail(@QueryParam("email") String email) {
        try {
            if (email == null || email.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("El parámetro de búsqueda es requerido")
                        .build();
            }
            UserDTO user = userService.getUserByEmail(email);
            return Response.ok(user).build();
        } catch (CustomException ex) {
            return RestExceptionHandler.handleCustomException(ex);
        } catch (Exception e) {
            return RestExceptionHandler.unexpectedCustomException(e);
        }
    }

    /**
     * Busca usuarios por estatus
     *
     * @param status estatus del usuario a buscar
     * @param type_user tipo de usuario a buscar
     * @return respuesta con la lista de usuarios
     */
    @GET
    @Path("/status")
    public Response getUsersByStatus(
            @QueryParam("status") String status,
            @QueryParam("type_user") String type_user) {
        try {
            if (status == null || status.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("El parámetro status de búsqueda es requerido")
                        .build();
            }
            List<UserDTO> users = userService.getUsersByStatus(status, type_user);
            return Response.ok(users).build();
        } catch (CustomException ex) {
            return RestExceptionHandler.handleCustomException(ex);
        } catch (Exception e) {
            return RestExceptionHandler.unexpectedCustomException(e);
        }
    }

    /**
     * Busca empleados por departamento
     *
     * @param department departamento del empleado a buscar
     * @return respuesta con la lista de empleados
     */
    @GET
    @Path("/department")
    public Response getEmployeesByDepartment(@QueryParam("department") String department) {
        try {
            if (department == null || department.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("El parámetro status de búsqueda es requerido")
                        .build();
            }
            List<UserDTO> users = userService.getEmployeeByDepartment(department);
            return Response.ok(users).build();
        } catch (CustomException ex) {
            return RestExceptionHandler.handleCustomException(ex);
        } catch (Exception e) {
            return RestExceptionHandler.unexpectedCustomException(e);
        }
    }

    /**
     * Busca empleados por departamento
     *
     * @param service servicio del proveedor a buscar
     * @return respuesta con la lista de proveedores
     */
    @GET
    @Path("/service")
    public Response getProvidersByService(@QueryParam("service") String service) {
        try {
            if (service == null || service.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("El parámetro status de búsqueda es requerido")
                        .build();
            }
            List<UserDTO> users = userService.getProvidersByService(service);
            return Response.ok(users).build();
        } catch (CustomException ex) {
            return RestExceptionHandler.handleCustomException(ex);
        } catch (Exception e) {
            return RestExceptionHandler.unexpectedCustomException(e);
        }
    }

    /**
     * Busca empleados por texto
     *
     * @param textSearch texto a buscar
     * @return respuesta con la lista de empleados
     */
    @GET
    @Path("/search")
    public Response searchEmployeesByText(@QueryParam("textSearch") String textSearch) {
        try {
            if (textSearch == null || textSearch.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("El parámetro de búsqueda es requerido")
                        .build();
            }

            List<UserDTO> users = userService.searchEmployeesByName(textSearch);
            return Response.ok(users).build();
        } catch (CustomException ex) {
            return RestExceptionHandler.handleCustomException(ex);
        } catch (Exception e) {
            return RestExceptionHandler.unexpectedCustomException(e);
        }
    }

    /**
     * Actualiza un usuario existente
     *
     * @param id identificador del usuario
     * @param user datos actualizados
     * @return respuesta con el usuario actualizado o error
     */
    @PUT
    @Path("/{id}")
    public Response updateUser(@PathParam("id") String id, UserModel user) {
        try {
            userService.updateUser(id, user);
            return Response.status(Response.Status.OK)
                    .entity(Map.of("message", "Usuario actualizado satisfactoriamente"))
                    .build();
        } catch (CustomException ex) {
            return RestExceptionHandler.handleCustomException(ex);
        } catch (Exception e) {
            return RestExceptionHandler.unexpectedCustomException(e);
        }
    }

    /**
     * Elimina un usuario
     *
     * @param id identificador del usuario
     * @return respuesta de éxito o error
     */
    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") String id) {
        try {
            userService.deleteUser(id);
            return Response.status(Response.Status.OK)
                    .entity(Map.of("message", "Usuario eliminado satisfactoriamente"))
                    .build();
        } catch (CustomException ex) {
            return RestExceptionHandler.handleCustomException(ex);
        } catch (Exception e) {
            return RestExceptionHandler.unexpectedCustomException(e);
        }
    }
}
