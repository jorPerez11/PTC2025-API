package H2C_Group.H2C_API.Models.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

@ToString
@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor //Constructor sin argumentos para deserealizacion de JSON
public class TicketStatusDTO {
    @Positive(message = "El id debe ser de valor positivo")
    private Long id;

    @NotBlank(message = "El nombre de visualizacion es obligatorio")
    private String displayName;

    public TicketStatusDTO(Long id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }
}
