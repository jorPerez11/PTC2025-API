package H2C_Group.H2C_API.Repositories;


import H2C_Group.H2C_API.Entities.SurveyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyRepository extends JpaRepository<SurveyEntity,Long> {

}
