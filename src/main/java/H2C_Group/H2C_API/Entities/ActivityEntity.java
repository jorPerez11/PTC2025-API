package H2C_Group.H2C_API.Entities;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name="TBACTIVITIES")

@ToString
@EqualsAndHashCode
@Getter
@Setter
public class ActivityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_Activities")
    @SequenceGenerator(name = "seq_Activities", sequenceName = "SEQ_ACTIVITIES", allocationSize = 1)
    @Column(name="ACTIVITY_ID")
    private Long activityId;
    @Column(name="ACTIVITYTITLE")
    private String activityTitle;
    @Column(name="ACTIVITYDESCRIPTION")
    private String activityDescription;
}
