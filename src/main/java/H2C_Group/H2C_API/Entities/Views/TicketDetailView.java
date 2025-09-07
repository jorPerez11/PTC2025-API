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
@Table(name = "VW_TICKETDETAILS")
public class TicketDetailView {
    @Id
    @Column(name = "TICKET_ID")
    private Long ticketId;

    @Column(name = "SOLICITANTE")
    private String solicitante;

    @Column(name = "ROL_SOLICITANTE")
    private String rol_solicitante;

    @Column(name = "FECHA_CREACION")
    private Date fecha_creacion;

    @Column(name = "TECNICO_ENCARGADO")
    private String tecnico_encargado;

    @Column(name = "ESTADO_TICKET")
    private String estado_ticket;
}
