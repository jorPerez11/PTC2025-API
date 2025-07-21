package H2C_Group.H2C_API.Models.DTO;

import jakarta.validation.constraints.NotBlank;
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
public class CommentDTO {
    private Long commentId;

    @NotNull(message = "El id del ticket es obligatorio.")
    private Long ticketId;

    @NotNull(message = "El id del usuario es obligatorio.")
    private Long userId;

    @NotBlank(message = "El mensaje es obligatorio.")
    @Size(max = 400, message = "El mensaje no puede exceder los 400 caracteres.")
    private String message;

}
