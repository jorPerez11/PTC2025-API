package H2C_Group.H2C_API.Entities.Views;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Immutable;

import java.util.Date;

@Entity
@Immutable
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Table(name = "VW_FULLTICKETDATA")
public class TicketView {

    @Id
    @Column(name = "TICKET_ID")
    private Long ticketId;

    @Column(name = "ASUNTO")
    private String asunto;

    @Column(name = "FULL_NAME")
    private String fullName;

    @Column(name = "CONSULT_DATE")
    private Date consultDate;

    @Column(name = "TICKET_STATUS")
    private String ticketStatus;

    @Column(name = "PHOTO_URL")
    private String photoUrl;

    @Column(name = "ASSIGNED_TECH") // Agrega esta línea
    private Long assignedTech;
}
