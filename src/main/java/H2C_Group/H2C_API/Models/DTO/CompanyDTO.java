package H2C_Group.H2C_API.Models.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.URL;

@ToString @EqualsAndHashCode
@Getter @Setter
public class CompanyDTO {
    private Long id;

    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres.")
    private String companyName;

    @NotBlank(message = "El correo es obligatorio.")
    @Size(max = 100, message = "El correo no puede exceder los 100 caracteres.")
    @Email(message = "Debe ser un correo válido.")
    private String emailCompany;


    @Size(max = 20, message = "El número de teléfono no puede exceder los 20 caracteres.")
    @Pattern(regexp = "^\\+?[0-9\\s\\-\\(\\)]{8,20}$", message = "El número de teléfono debe ser válido.")
    private String contactPhone;

    @URL(message = "La URL de la web debe ser válida.")
    @Size(max = 200, message = "La URL no puede exceder los 200 caracteres.")
    private String websiteUrl;

}
