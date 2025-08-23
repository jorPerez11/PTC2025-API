package H2C_Group.H2C_API.Repositories;

import H2C_Group.H2C_API.Entities.SolutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolutionRepository extends JpaRepository<SolutionEntity,Long> {
    @Query("SELECT s FROM SolutionEntity s " +
            "WHERE LOWER(s.solutionTitle) LIKE LOWER(CONCAT('%', :value, '%')) " +
            "   OR LOWER(s.keyWords) LIKE LOWER(CONCAT('%', :value, '%'))")
    List<SolutionEntity> searchBySolutionTitleOrKeyWords(@Param("value") String value);
}
