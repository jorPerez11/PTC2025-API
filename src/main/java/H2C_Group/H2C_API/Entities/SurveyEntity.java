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
@Table(name="TBSURVEYS")

@ToString(exclude = {"user", "ticket"})
@EqualsAndHashCode(exclude = {"user", "ticket"})
@Getter
@Setter
public class SurveyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "solution_seq_generator")
    @SequenceGenerator(name = "solution_seq_generator", sequenceName = "SEQ_SURVEYSID", allocationSize = 1)
    @Column(name="SURVEYID")
    private Long surveyId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USERID", nullable = false)
    private UserEntity user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TICKETID", nullable = false) // FK a tbTickets.ticketId. No puede ser nulo.
    private TicketEntity ticket;
    @Column(name="SCORE")
    private Integer score;
    @Column(name="COMMENTSURVEY")
    private String commentSurvey;
    @CreationTimestamp
    @Column(name="SUBMITDATE")
    private LocalDateTime submitDate;
}
