package H2C_Group.H2C_API.Models.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Getter
@Setter
public class ActivityDTO {
    private Long  id;

    @NotBlank(message = "El titulo no debe ser nulo.")
    private String activityTitle;

    @NotBlank(message = "La descripcion no debe ser nula.")
    private String activityDescription;

}
