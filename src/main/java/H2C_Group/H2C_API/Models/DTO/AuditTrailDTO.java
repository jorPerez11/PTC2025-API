package H2C_Group.H2C_API.Models.DTO;

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
public class AuditTrailDTO {
    private Long auditTrailId;

    @NotNull(message = "El id del ticket es obligatorio.")
    private Long ticketId;

    @NotNull(message = "El id del usuario es obligatorio.")
    private Long userId;

    @Size(max = 50, message = "El valor cambiado no puede exceder los 50 caracteres.")
    private String changedField;

    @Size(max = 50, message = "El valor previo no puede exceder los 50 caracteres.")
    private String previousValue;

    @Size(max = 50, message = "El nuevo valor no puede exceder los 50 caracteres")
    private String newValue;

}
