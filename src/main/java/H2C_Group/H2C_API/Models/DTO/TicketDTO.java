package H2C_Group.H2C_API.Models.DTO;


import H2C_Group.H2C_API.Enums.Category;
import H2C_Group.H2C_API.Enums.TicketPriority;
import H2C_Group.H2C_API.Enums.TicketStatus;
import H2C_Group.H2C_API.Enums.TicketField;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@ToString @EqualsAndHashCode
@Getter @Setter
public class TicketDTO {
    private Long ticketId;

    @NotNull(message = "La categoria es obligatoria.")
    private CategoryDTO category;

    @NotNull(message = "La prioridad es obligatoria.")
    private TicketPriorityDTO priority;

    //Estado del ticket. Se asigna automaticamente (En espera, En progreso) a excepcion del estado "Completado"
    private TicketStatusDTO status;

    //CLIENTE
    @NotNull(message = "El id del usuario es obligatorio.")
    private Long userId;

    @NotBlank(message = "El título es obligatorio.")
    @Size(max = 100, message = "El título no puede exceder los 200 caracteres.")
    private String title;

    @NotBlank(message = "La descripción es obligatoria.")
    @Size(max = 200, message = "La descripción no puede exceder el límite de 200 caracteres.")
    private String description;

    //TECNICO
    private Long assignedTech;

    private LocalDateTime creationDate;

    //CAMPO closeDate
    private LocalDateTime closeDate;

}
