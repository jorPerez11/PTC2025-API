package H2C_Group.H2C_API.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "TBCATEGORY")
@Getter @Setter
@ToString
@EqualsAndHashCode
public class CategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_CategoryId")
    @SequenceGenerator(name = "seq_CategoryId", sequenceName = "SEQ_CATEGORYID", allocationSize = 1)
    @Column(name = "CATEGORYID")
    private Long categoryId;

    @Column(name = "CATEGORYNAME", nullable = false, length = 50)
    private String categoryName;


    public CategoryEntity(String categoryName) {
        this.categoryName = categoryName;
    }

    // Constructor por defecto (necesario para JPA/Hibernate)
    public CategoryEntity() {
    }

    // Getters y Setters
    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
