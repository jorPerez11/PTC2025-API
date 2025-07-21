package H2C_Group.H2C_API.Repositories;

import H2C_Group.H2C_API.Entities.NotificationEntity;
import H2C_Group.H2C_API.Models.DTO.NotificationDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

}
