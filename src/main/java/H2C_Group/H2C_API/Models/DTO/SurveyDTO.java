package H2C_Group.H2C_API.Models.DTO;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Getter
@Setter
public class SurveyDTO {
    private Long surveyId;

    @NotNull(message = "El id del ticket es obligatorio.")
    private Long ticketId;

    @NotNull(message = "El id del usuario es obligatorio.")
    private Long userId;

    @NotNull(message = "El score es obligatorio.") // Si el score NO puede ser nulo
    @Min(value = 1, message = "El score debe ser al menos 1.") // Valor mínimo permitido
    @Max(value = 5, message = "El score no puede exceder 5.")   // Valor máximo permitido
    private Integer score;

    @Size(max = 400, message = "El comentario no puede exceder los 400 caracteres.")
    private String commentSurvey;

}
