package H2C_Group.H2C_API.Entities;


import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="TBUSERS")

@ToString(exclude = {"company"}) //Entidades relacionadas
@EqualsAndHashCode(exclude = {"company"})
@Getter
@Setter
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq_generator")
    @SequenceGenerator(name = "user_seq_generator", sequenceName = "SEQ_USERSID", allocationSize = 1)    @Column(name="USERID")
    private Long userId;
    @Column(name="ROLID")
    private Long rolId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMPANYID", nullable = false)
    private CompanyEntity company;
    @Column(name="CATEGORYID")
    private Long categoryId;
    @Column(name="FULLNAME")
    private String fullName;
    @Column(name="USERNAME")
    private String username;
    @Column(name="EMAIL")
    private String email;
    @Column(name="PHONE")
    private String phone;
    @Column(name="PASSWORDHASH")
    private String passwordHash;
//    @Column(name="TOKENSESION")
//    private String tokenSesion;
    @Column(name="ISACTIVE")
    private Integer isActive;
    @CreationTimestamp
    @Column(name="REGISTRATIONDATE")
    private LocalDateTime registrationDate;
}
