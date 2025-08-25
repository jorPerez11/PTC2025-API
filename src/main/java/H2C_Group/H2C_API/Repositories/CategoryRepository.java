package H2C_Group.H2C_API.Repositories;

import H2C_Group.H2C_API.Entities.CategoryEntity;
import H2C_Group.H2C_API.Entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
}
