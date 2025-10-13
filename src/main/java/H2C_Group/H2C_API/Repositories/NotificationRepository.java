package H2C_Group.H2C_API.Repositories;

import H2C_Group.H2C_API.Entities.NotificationEntity;
import H2C_Group.H2C_API.Entities.UserEntity;
import H2C_Group.H2C_API.Models.DTO.NotificationDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.management.Notification;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Spring Data JPA genera la consulta:
     * SELECT * FROM TBNOTIFICATIONS WHERE USERID = :user_id
     * ORDER BY NOTIFICATIONDATE DESC
     * * Nota: El nombre del método usa 'User' porque así se llama la propiedad en la Entidad.
     */
    List<NotificationEntity> findByUserOrderByNotificationDateDesc(UserEntity user);
}
