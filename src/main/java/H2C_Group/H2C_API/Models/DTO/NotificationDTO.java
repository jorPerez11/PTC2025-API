package H2C_Group.H2C_API.Models.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Getter
@Setter
public class NotificationDTO {
    private Long notificationId;

    @NotNull(message = "El id del ticket es obligatorio.")
    private Long ticketId;

    @NotNull(message = "El id del usuario es obligatorio.")
    private Long userId;

    @NotBlank(message = "El mensaje es obligatorio.")
    private String message;

    @NotNull(message = "El estado es obligatorio.")
    @Min(value = 0, message = "El estado 'seen' debe ser 0 (inactivo) o 1 (activo).")
    @Max(value = 1, message = "El estado 'seen' debe ser 0 (inactivo) o 1 (activo).")
    private Integer seen;

}
