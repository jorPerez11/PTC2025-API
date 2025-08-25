package H2C_Group.H2C_API.Entities;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="TBSOLUTION")

@ToString(exclude = {"user"})
@EqualsAndHashCode(exclude = {"user"})
@Getter
@Setter
public class SolutionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "solution_seq_generator")
    @SequenceGenerator(name = "solution_seq_generator", sequenceName = "SEQ_SOLUTIONID", allocationSize = 1)
    @Column(name="SOLUTIONID")
    private Long solutionId;
    @Column(name="SOLUTIONTITLE")
    private String solutionTitle;
    @Column(name="DESCRIPTIONS")
    private String descriptionS;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USERID", nullable = false)
    private UserEntity user;
    @Column(name="SOLUTIONSTEPS")
    private String solutionSteps;
    @Column(name="KEYWORDS")
    private String keyWords;
    @UpdateTimestamp
    @Column(name="UPDATEDATE")
    private LocalDateTime updateDate;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORYID")
    private CategoryEntity category;
}
