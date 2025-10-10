package H2C_Group.H2C_API.Models.DTO;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Date;
import java.sql.Timestamp;

@ToString
@EqualsAndHashCode
@Getter
@Setter
public class TicketViewDTO {

    private Long ticketId;
    private String asunto;
    private String fullName;
    private Timestamp consultDate;
    private String ticketStatus;
    private String photoUrl;

    public TicketViewDTO() {}

    // Constructor que mapea directamente de la vista (o de la entidad tbTickets)
    public TicketViewDTO(Long ticketId, String asunto, String fullName, Timestamp consultDate, String ticketStatus, String photoUrl) {
        this.ticketId = ticketId;
        this.asunto = asunto;
        this.fullName = fullName;
        this.consultDate = consultDate;
        this.ticketStatus = ticketStatus;
        this.photoUrl = photoUrl;
    }
}
