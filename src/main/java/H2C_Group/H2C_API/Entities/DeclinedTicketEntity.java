package H2C_Group.H2C_API.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TBDECLINEDTICKETS")
@IdClass(DeclinedTicketEntity.class)
public class DeclinedTicketEntity implements Serializable {

    @Id
    @Column(name = "TICKETID")
    private Long ticketId;

    @Id
    @Column(name = "TECHNICIANID")
    private Long technicianId;
}
