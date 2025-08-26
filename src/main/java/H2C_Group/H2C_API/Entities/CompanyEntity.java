package H2C_Group.H2C_API.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="TBCOMPANIES")

@ToString(exclude = {"users"})
@EqualsAndHashCode(exclude = {"users"})
@Getter
@Setter
public class CompanyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "company_seq_generator")
    @SequenceGenerator(name = "company_seq_generator", sequenceName = "SEQ_COMPANIESID", allocationSize = 1)
    @Column(name="COMPANYID")
    private Long companyId;
    @Column(name="COMPANYNAME")
    private String companyName;
    @Column(name="EMAILCOMPANY")
    private String emailCompany;
    @Column(name="CONTACTPHONE")
    private String contactPhone;
    @Column(name="WEBSITEURL")
    private String websiteUrl;
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<UserEntity> users = new ArrayList<>();

}
