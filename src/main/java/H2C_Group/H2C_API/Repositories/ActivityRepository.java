package H2C_Group.H2C_API.Repositories;

import H2C_Group.H2C_API.Entities.ActivityEntity;
import H2C_Group.H2C_API.Entities.AuditTrailEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRepository extends JpaRepository<ActivityEntity, Long> {
}
