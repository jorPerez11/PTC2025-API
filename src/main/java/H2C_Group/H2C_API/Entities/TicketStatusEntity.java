package H2C_Group.H2C_API.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "TBTICKETSTATUS")
@Getter
@Setter
public class TicketStatusEntity {
    @Id
    @Column(name = "TICKETSTATUSID")
    private Integer ticketStatusId;

    @Column(name = "STATUS")
    private String status;
}
