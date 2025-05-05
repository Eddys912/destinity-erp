package com.destinity.erp.hr;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

/**
 * Modelo que representa un usuario dentro del sistema.
 * Este modelo esta diseñado para ser almacenado en MongoDb como documento.
 * Contiene informacion del usuario, del empleado o del proveedor.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserModel {

    private ObjectId id;
    private String firstName;
    private String lastName;
    private String middleName;
    private String email;
    private String password;
    private String userType;
    private String status;
    private EmployeeData employeeData;
    private ProviderData providerData;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Subdocumento que almacena los datos del empleado.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeData {

        private String role;
        private String department;
        private Double salary;
    }

    /**
     * Subdocumento que almacena los datos del proveedor.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProviderData {

        private String company;
        private String serviceType;
        private String phone;
    }
}
