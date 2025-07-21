package H2C_Group.H2C_API.Models.DTO;


import H2C_Group.H2C_API.Enums.Category;
import H2C_Group.H2C_API.Enums.UserRole;
import jakarta.validation.constraints.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

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

        @Size(max = 9, message = "El número de teléfono no puede exceder los 9 caracteres.")
        @Pattern(regexp = "^[0-9]{9}$", message = "El número de teléfono debe contener exactamente 9 dígitos.")
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
        @NotNull(message = "El rol del usuario es obligatorio.")
        private RolDTO rol;

        @NotNull(message = "La compañia es obligatoria.")
        private Long companyId;

        //OPCIONAL: Categoria solo asignable a Roles Tecnico y Administrador

        private CategoryDTO category;

}
