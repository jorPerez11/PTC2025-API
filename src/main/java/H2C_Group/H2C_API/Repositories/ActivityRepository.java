package H2C_Group.H2C_API.Repositories;

import H2C_Group.H2C_API.Entities.ActivityEntity;
import H2C_Group.H2C_API.Entities.AuditTrailEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityRepository extends JpaRepository<ActivityEntity, Long> {
    @Query("SELECT a FROM ActivityEntity a WHERE " +
            "LOWER(a.activityTitle) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.activityDescription) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<ActivityEntity> findBySearchTerm(@Param("search") String search, Pageable pageable);
}
