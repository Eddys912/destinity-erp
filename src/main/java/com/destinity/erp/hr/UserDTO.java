package com.destinity.erp.hr;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO expone información resumida de un usuario a traves de la API.
 * Se emplea para mostrar los datos necesarios en el frontend o sistemas
 * externos, sin incluir la estructura completa del modelo.
 */
@Data
@NoArgsConstructor
public class UserDTO {

    private String id;
    private String firstName;
    private String lastName;
    private String middleName;
    private String email;
    private String userType;
    private String role;
    private String department;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserDTO(UserModel user) {
        this.id = (user.getId() != null)
                ? user.getId().toHexString()
                : null;
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.middleName = user.getMiddleName();
        this.email = user.getEmail();
        this.userType = user.getUserType();
        this.role = (user.getEmployeeData() != null)
                ? user.getEmployeeData().getRole()
                : null;
        this.department = (user.getEmployeeData() != null)
                ? user.getEmployeeData().getDepartment()
                : null;
        this.status = user.getStatus();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }
}
