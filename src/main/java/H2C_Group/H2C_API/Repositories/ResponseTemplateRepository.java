package H2C_Group.H2C_API.Repositories;


import H2C_Group.H2C_API.Entities.ResponseTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResponseTemplateRepository extends JpaRepository<ResponseTemplateEntity,Long> {
    boolean existsByCategory_CategoryId(Long categoryId);
}
