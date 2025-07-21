package H2C_Group.H2C_API.Entities;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name="TBRESPONSETEMPLATES")

@ToString
@EqualsAndHashCode
@Getter
@Setter
public class ResponseTemplateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "solution_seq_generator")
    @SequenceGenerator(name = "solution_seq_generator", sequenceName = "SEQ_RESPONSETEMPLATESID", allocationSize = 1)
    @Column(name="TEMPLATEID")
    private Long templateId;
    @Column(name="CATEGORYID")
    private Long categoryId;
    @Column(name="TITLE")
    private String title;
    @Column(name="TEMPLATECONTENT")
    private String templateContent;
    @Column(name="KEYWORDS")
    private String keywords;

}
