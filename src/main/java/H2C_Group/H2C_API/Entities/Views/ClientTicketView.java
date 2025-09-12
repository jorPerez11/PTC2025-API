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
@Table(name = "VW_CLIENTTICKETS")
public class ClientTicketView {

    @Id
    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "NOMBRE_CLIENTE")
    private String nombre_cliente;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "FECHA_REGISTRO")
    private Date fecha_registro;

    @Column(name = "TICKET_ID")
    private Long ticketId;

    @Column(name = "ESTADO_SOLICITUD")
    private String estado_solicitud;
}
