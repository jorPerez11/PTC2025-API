package H2C_Group.H2C_API.Entities;


import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="TBTICKETS")

@ToString
@EqualsAndHashCode
@Getter
@Setter
public class TicketEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ticket_seq_generator")
    @SequenceGenerator(name = "ticket_seq_generator", sequenceName = "SEQ_TICKETSID", allocationSize = 1)
    @Column(name="TICKETID")
    private Long ticketId;
    @Column(name="CATEGORYID")
    private Long categoryId;
    @Column(name="PRIORITYID")
    private Long priorityId;
    @Column(name="TICKETSTATUSID")
    private Long ticketStatusId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USERID", nullable = false) // El usuario es obligatorio para la creacion del ticket
    private UserEntity userCreator;
    @Column(name="TITLE")
    private String title;
    @Column(name="DESCRIPTION")
    private String description;
    @ManyToOne(fetch = FetchType.LAZY) // Carga perezosa
    @JoinColumn(name = "ASSIGNEDTECH", nullable = true) // El tecnico asignado es nulo al crear el ticket.
    private UserEntity assignedTechUser;
    @CreationTimestamp
    @Column(name="CREATIONDATE")
    private LocalDateTime creationDate;
    @Column(name="CLOSEDATE")
    private LocalDateTime closeDate;
}
