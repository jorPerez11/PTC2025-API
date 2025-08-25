package H2C_Group.H2C_API.Models.DTO;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;

@ToString
@EqualsAndHashCode
@Getter
@Setter
public class SolutionDTO {

    private Long solutionId;

    @NotNull(message = "La categoria es obligatoria.")
    private CategoryDTO category;

    @NotBlank(message = "El titulo es obligatorio.")
    @Size(max = 100, message = "El titulo no puede exceder los 100 caracteres.")
    private String solutionTitle;

    @NotBlank(message = "La descripcion es obligatoria.")
    @Size(max = 400, message = "La descripcion no puede exceder los 400 caracteres.")
    private String descriptionS;

    @NotNull(message = "El id del usuario es obligatorio.")
    private Long userId;

    @Size(max = 500, message = "Los pasos no pueden exceder los 500 caracteres.")
    private String solutionSteps;

    @Size(max = 50, message = "La clave no puede exceder los 50 caracteres")
    private String keyWords;

    private LocalDateTime updateDate;
}
