package H2C_Group.H2C_API.Models.DTO;


import H2C_Group.H2C_API.Enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString @EqualsAndHashCode
@Getter @Setter
public class RolDTO {

    @Positive(message = "El id debe ser un valor positivo")
    private Long id;

    @NotBlank(message = "El nombre de visualizacion es obligatorio")
    private String displayName;

    public RolDTO() {
        // Constructor vacío necesario para la deserialización de Jackson
    }

    public RolDTO(UserRole userRoleEnum) {
        this.id = userRoleEnum.getId();
        this.displayName = userRoleEnum.getDisplayName();
    }
}
