package com.destinity.erp.hr;

import com.destinity.erp.database.DataBaseConnection;
import com.destinity.erp.utils.CustomException;
import com.destinity.erp.utils.ToDate;
import com.mongodb.MongoException;
import com.mongodb.MongoWriteException;
import com.mongodb.WriteError;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Repositorio encargado de la interacción directa con MongoDb.
 * Administra la conversión entre el modelo de usuarios y los
 * documentos almacenados en la base de datos.
 */
@ApplicationScoped
public class UserRepository {

    private static final Logger LOGGER = Logger.getLogger(UserRepository.class.getName());
    private static final String COLLECTION_NAME = "hr";

    @Inject
    private DataBaseConnection dbConnection;

    /**
     * Obtiene la colección de usuarios
     *
     * @return MongoCollection de usuarios
     */
    private MongoCollection<Document> getUserCollection() {
        return dbConnection.getDatabase().getCollection(COLLECTION_NAME);
    }

    /**
     * Guarda un nuevo usuario en la base de datos
     *
     * @param user modelo de usuario a guardar
     * @return ID del usuario guardado o null si falla
     */
    public String saveUser(UserModel user) {
        try {
            Document userDoc = userToDocument(user);
            InsertOneResult result = getUserCollection().insertOne(userDoc);
            return result.getInsertedId().asObjectId().getValue().toString();
        } catch (MongoWriteException e) {
            WriteError writeError = e.getError();
            if (writeError != null && writeError.getCode() == 121) {
                LOGGER.log(Level.WARNING, "El documento no cumple con el esquema definido: {0}", e.getMessage());
                throw CustomException.dbValidationFailed("El documento no cumple con el esquema definido.");
            } else {
                LOGGER.log(Level.SEVERE, "Error al insertar al usuario en la base de datos: {0}", e.getMessage());
                throw CustomException.dbError("Error al insertar al usuario en la base de datos.");
            }
        } catch (MongoException ex) {
            LOGGER.log(Level.SEVERE, "Error general en MongoDB: {0}", ex.getMessage());
            throw CustomException.dbError("Error general en MongoDB.");
        }
    }

    /**
     * Busca todos los empleados o proveedores (con paginación)
     *
     * @param page número de página (empezando desde 0)
     * @param pageSize tamaño de página
     * @param userType tipo de usuario
     * @return Lista de usuarios
     */
    public List<UserModel> findAllUsers(int page, int pageSize, String userType) {
        try {
            Bson filter = new Document();
            if (userType != null && !userType.isBlank()) 
                filter = Filters.eq("userType", userType);

            List<UserModel> users = new ArrayList<>();
            FindIterable<Document> documents = getUserCollection()
                    .find(filter)
                    .skip(page * pageSize)
                    .limit(pageSize);

            for (Document doc : documents) users.add(documentToUser(doc));
            return users;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener usuarios - Page" + page + ", Size" + pageSize + " {0}", e);
            throw CustomException.dbError("Error al obtener usuarios.");
        }
    }

    /**
     * Busca un usuario por su ID
     *
     * @param id identificador del usuario
     * @return Optional con el usuario encontrado o vacío si no existe
     */
    public Optional<UserModel> findUserById(String id) {
        try {
            if (id == null || !ObjectId.isValid(id)) {
                LOGGER.log(Level.WARNING, "ID inválido recibido: {0}", id);
                return Optional.empty();
            }
            ObjectId objectId = new ObjectId(id);
            Document doc = getUserCollection().find(Filters.eq("_id", objectId)).first();
            return Optional.ofNullable(documentToUser(doc));
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener usuario ID: " + id + " {0}", e);
            throw CustomException.dbError("Error al obtener usuario.");
        }
    }

    /**
     * Busca un usuario por su correo
     *
     * @param email correo del usuario
     * @return Optional con el usuario encontrado o vacío si no existe
     */
    public Optional<UserModel> findUserByEmail(String email) {
        try {
            Document doc = getUserCollection()
                    .find(Filters.eq("email", email)).first();
            return Optional.ofNullable(documentToUser(doc));
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener correo: " + email + " {0}", e);
            throw CustomException.dbError("Error al obtener email.");
        }
    }

    /**
     * Busca empleados por estatus
     *
     * @param status del usuario
     * @param userType tipo de usuario
     * @return Lista de usuarios según el estatus
     */
    public List<UserModel> findUserByStatus(String status, String userType) {
        try {
            List<UserModel> users = new ArrayList<>();
            FindIterable<Document> documents = getUserCollection()
                    .find(Filters.and(
                            Filters.eq("status", status),
                            Filters.eq("userType", userType)
                    ));

            for (Document doc : documents) users.add(documentToUser(doc));
            return users;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener usuarios del estatus: " + status + " {0}", e);
            throw CustomException.dbError("Error al obtener usuarios del estatus: " + status);
        }
    }

    /**
     * Busca empleados por departamento
     *
     * @param department departamento al que pertenece el empleado
     * @return Lista de empleados del departamento especificado
     */
    public List<UserModel> findEmployeeByDepartment(String department) {
        try {
            List<UserModel> users = new ArrayList<>();
            FindIterable<Document> documents = getUserCollection()
                    .find(Filters.eq("employeeData.department", department));

            for (Document doc : documents) users.add(documentToUser(doc));
            return users;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener empleados del departamento: " + department + " {0}", e);
            throw CustomException.dbError("Error al obtener empleados del departamento: " + department);
        }
    }

    /**
     * Busca proveedores por tipo de servicio
     *
     * @param serviceType tipo de servicio del proveedor
     * @return Lista de proveedores del servicio especificado
     */
    public List<UserModel> findProviderByServiceType(String serviceType) {
        try {
            List<UserModel> users = new ArrayList<>();
            FindIterable<Document> documents = getUserCollection()
                    .find(Filters.eq("providerData.serviceType", serviceType));

            for (Document doc : documents) users.add(documentToUser(doc));
            return users;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener proveedores del servicio: " + serviceType + " {0}", e);
            throw CustomException.dbError("Error al obtener proveedores del servicio: " + serviceType);
        }
    }

    /**
     * Busca usuarios por texto (búsqueda parcial)
     *
     * @param searchText parte del texto a buscar
     * @param userType tipo de usuario
     * @return Lista de usuarios que contienen el texto
     */
    public List<UserModel> findUsersByText(String searchText, String userType) {
        try {
            List<UserModel> users = new ArrayList<>();
            String searchFilter = ".*" + Pattern.quote(searchText) + ".*";
            Bson filter = Filters.and(
                    Filters.eq("userType", userType),
                    Filters.or(
                            Filters.regex("firstName", searchFilter, "i"),
                            Filters.regex("lastName", searchFilter, "i"),
                            Filters.regex("middleName", searchFilter, "i"),
                            Filters.regex("email", searchFilter, "i"))
            );
            FindIterable<Document> documents = getUserCollection().find(filter);

            for (Document doc : documents) users.add(documentToUser(doc));
            return users;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener usuarios: " + searchText + " {0}", e);
            throw CustomException.dbError("Error al obtener usuarios: " + searchText);
        }
    }

    /**
     * Actualiza una usuario existente
     *
     * @param user modelo con los datos actualizados
     * @return ID del usuario actualizado o null si falla
     */
    public String updateUser(UserModel user) {
        try {
            Document updateDoc = userToDocument(user);
            UpdateResult result = getUserCollection().updateOne(
                    Filters.eq("_id", user.getId()),
                    new Document("$set", updateDoc)
            );
            return result.getModifiedCount() > 0 ? user.getId().toString() : null;
        } catch (MongoWriteException e) {
            WriteError writeError = e.getError();
            if (writeError != null && writeError.getCode() == 121) {
                LOGGER.log(Level.WARNING, "El documento no cumple con el esquema definido: {0}", e.getMessage());
                throw CustomException.dbValidationFailed("El documento no cumple con el esquema definido.");
            } else {
                LOGGER.log(Level.SEVERE, "Error al actualizar el usuario: {0}", e.getMessage());
                throw CustomException.dbError("Error al actualizar el usuario.");
            }
        } catch (MongoException ex) {
            LOGGER.log(Level.SEVERE, "Error general en MongoDB: {0}", ex.getMessage());
            throw CustomException.dbError("Error general en MongoDB.");
        }
    }

    /**
     * Elimina una usuario por su ID
     *
     * @param id identificador de la usuario a eliminar
     * @return true si se eliminó correctamente
     */
    public boolean deleteUser(String id) {
        try {
            ObjectId objectId = new ObjectId(id);
            DeleteResult result = getUserCollection().deleteOne(Filters.eq("_id", objectId));
            return result.getDeletedCount() > 0;
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Error al eliminar usuario: " + id + " {0}", e);
            throw CustomException.dbError("Error al eliminar usuario.");
        } catch (MongoException ex) {
            LOGGER.log(Level.SEVERE, "Error general en MongoDB: {0}", ex.getMessage());
            throw CustomException.dbError("Error general en MongoDB.");
        }
    }

    /**
     * Convierte un UserModel a un Document de MongoDB
     *
     * @param user modelo de la usuario
     * @return Document para MongoDB
     */
    private Document userToDocument(UserModel user) {
        Document userDoc = new Document();
        Document employeeDoc = new Document();
        Document providerDoc = new Document();

        userDoc
                .append("_id", user.getId())
                .append("firstName", user.getFirstName())
                .append("lastName", user.getLastName())
                .append("middleName", user.getMiddleName())
                .append("email", user.getEmail())
                .append("password", user.getPassword())
                .append("userType", user.getUserType())
                .append("status", user.getStatus())
                .append("createdAt", ToDate.toDate(user.getCreatedAt()))
                .append("updatedAt", ToDate.toDate(user.getUpdatedAt()));

        if ("employee".equals(user.getUserType())) {
            employeeDoc
                    .append("role", user.getEmployeeData().getRole())
                    .append("department", user.getEmployeeData().getDepartment())
                    .append("salary", user.getEmployeeData().getSalary());
            userDoc.append("employeeData", employeeDoc);
        }

        if ("provider".equals(user.getUserType())) {
            providerDoc
                    .append("company", user.getProviderData().getCompany())
                    .append("serviceType", user.getProviderData().getServiceType())
                    .append("phone", user.getProviderData().getPhone());
            userDoc.append("providerData", providerDoc);
        }

        return userDoc;
    }

    /**
     * Convierte un Document de MongoDB a un UserModel
     *
     * @param doc Document de MongoDB
     * @return UserModel
     */
    private UserModel documentToUser(Document doc) {
        if (doc == null) return null;

        UserModel user = new UserModel();

        user.setId(doc.getObjectId("_id"));
        user.setFirstName(doc.getString("firstName"));
        user.setLastName(doc.getString("lastName"));
        user.setMiddleName(doc.getString("middleName"));
        user.setEmail(doc.getString("email"));
        user.setPassword(doc.getString("password"));
        user.setUserType(doc.getString("userType"));
        user.setStatus(doc.getString("status"));
        user.setCreatedAt(ToDate.toLocalDateTime(doc.getDate("createdAt")));
        user.setUpdatedAt(ToDate.toLocalDateTime(doc.getDate("updatedAt")));

        if (doc.containsKey("employeeData")) {
            Document emp = doc.get("employeeData", Document.class);
            user.setEmployeeData(new UserModel.EmployeeData(
                    emp.getString("role"),
                    emp.getString("department"),
                    emp.getDouble("salary")
            ));
        }
        if (doc.containsKey("providerData")) {
            Document prov = doc.get("providerData", Document.class);
            user.setProviderData(new UserModel.ProviderData(
                    prov.getString("company"),
                    prov.getString("serviceType"),
                    prov.getString("phone")
            ));
        }
        return user;
    }

    /**
     * Cuenta el total de usuarios
     *
     * @return número total de usuarios
     */
    public long countUsers() {
        try {
            return getUserCollection().countDocuments();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error general en MongoDB: {0}", e.getMessage());
            throw CustomException.dbError("Error general en MongoDB.");
        }
    }
}
