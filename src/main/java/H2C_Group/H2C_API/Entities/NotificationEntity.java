package H2C_Group.H2C_API.Entities;


import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="TBNOTIFICATIONS")

@ToString(exclude = {""})
@EqualsAndHashCode(exclude = {""})
@Getter
@Setter
public class NotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "solution_seq_generator")
    @SequenceGenerator(name = "solution_seq_generator", sequenceName = "SEQ_NOTIFICATIONSID", allocationSize = 1)
    @Column(name="NOTIFICATIONID")
    private Long notificationId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USERID", nullable = false)
    private UserEntity user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TICKETID", nullable = false)
    private TicketEntity ticket;
    @Column(name="MESSAGE")
    private String message;
    @Column(name="SEEN")
    private Integer seen;
    @CreationTimestamp
    @Column(name="NOTIFICATIONDATE")
    private LocalDateTime notificationDate;

}
