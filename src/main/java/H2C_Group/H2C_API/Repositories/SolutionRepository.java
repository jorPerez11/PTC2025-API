package H2C_Group.H2C_API.Repositories;

import H2C_Group.H2C_API.Entities.SolutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolutionRepository extends JpaRepository<SolutionEntity,Long> {


}
