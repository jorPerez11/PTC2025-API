package H2C_Group.H2C_API.Models.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Getter
@Setter
public class ProfileDTO {
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    private String name;

    @Size(max = 100, message = "El correo no puede exceder los 100 caracteres.")
    @Email(message = "Debe ser un correo válido.")
    private String email;

    @Size(max = 20, message = "El número de teléfono no puede exceder los 20 caracteres.")
    private String phone;

    private String profilePictureUrl;
}
