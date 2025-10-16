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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NOTIFICATIONID")
    private Long notificationId;

    @Column(name = "USERID", nullable = false)
    private Long userId;

    @Column(name = "TICKETID")
    private Long ticketId;

    @Column(name = "MESSAGE", nullable = false)
    private String message;

    @Column(name = "SEEN")
    private Integer seen = 0;

    @Column(name = "NOTIFICATIONDATE", insertable = false, updatable = false)
    private LocalDateTime notificationDate;
}
