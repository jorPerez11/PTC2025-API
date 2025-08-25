package H2C_Group.H2C_API.Models.DTO;


import H2C_Group.H2C_API.Enums.Category;
import H2C_Group.H2C_API.Enums.UserRole;
import jakarta.validation.constraints.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

@ToString @EqualsAndHashCode
@Getter @Setter
public class UserDTO {
        private Long id;

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
        private String name;

        @NotBlank(message = "El usuario es obligatorio.")
        @Size(max = 100, message = "El usuario no puede exceder los 100 caracteres.")
        private String username;

        @NotBlank(message = "El correo es obligatorio.")
        @Size(max = 100, message = "El correo no puede exceder los 100 caracteres.")
        @Email(message = "Debe ser un correo válido.")
        private String email;

        @Size(max = 20, message = "El número de teléfono no puede exceder los 20 caracteres.")
        @Pattern(regexp = "^\\+?[0-9\\s\\-\\(\\)]{8,20}$", message = "El número de teléfono debe ser válido.")
        private String phone;


        @NotBlank(message = "La contraseña es obligatoria.")
        @Size(min = 8, max = 30, message = "La contraseña debe tener entre 8 y 30 caracteres.")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$", message = "La contraseña debe contener al menos una mayúscula, una minúscula, un número y un carácter especial.")
        private String password;

        @NotNull(message = "El estado es obligatorio.")
        @Min(value = 0, message = "El estado 'isActive' debe ser 0 (inactivo) o 1 (activo).")
        @Max(value = 1, message = "El estado 'isActive' debe ser 0 (inactivo) o 1 (activo).")
        private Integer isActive;

        //Variable de tipo UserRole - Obtiene el numero (rolId) de Enum UserRole.
        //Se usa por seguridad (datos constantes), legibilidad (de codigo) y serializacion
        private RolDTO rol;

        private Long companyId;

        //OPCIONAL: Categoria solo asignable a Roles Tecnico y Administrador

        private CategoryDTO category;

        @URL
        private String profilePictureUrl;

        private String displayName;


        private LocalDateTime registrationDate;


}
