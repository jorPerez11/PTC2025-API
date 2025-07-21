package H2C_Group.H2C_API.Entities;


import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="TBAUDITTRAIL")

@ToString(exclude = {"ticket", "user"})
@EqualsAndHashCode(exclude = {"ticket", "user"})
@Getter
@Setter
public class AuditTrailEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comment_seq_generator")
    @SequenceGenerator(name = "comment_seq_generator", sequenceName = "SEQ_AUDITTRAILID", allocationSize = 1)
    @Column(name="HISTORYID")
    private Long historyId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TICKETID", nullable = false)
    private TicketEntity ticket;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USERID", nullable = false)
    private UserEntity user;
    @Column(name="CHANGEDFIELD")
    private String changedField;
    @Column(name="PREVIOUSVALUE")
    private String previousValue;
    @Column(name="NEWVALUE")
    private String newValue;
    @CreationTimestamp
    @Column(name="CHANGEDATE")
    private LocalDateTime changeDate;



}
