package H2C_Group.H2C_API.Models.DTO;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AllUsersDTO {
    private String solicitante;
    private String rol;
    private String tecnicoEncargado;
    private String estadoDeTicket;
    private Date registroDate; // Fecha de registro del usuario
}
