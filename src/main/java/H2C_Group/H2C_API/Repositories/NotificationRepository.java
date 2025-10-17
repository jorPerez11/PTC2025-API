package H2C_Group.H2C_API.Repositories;

import H2C_Group.H2C_API.Entities.NotificationEntity;
import H2C_Group.H2C_API.Models.DTO.NotificationDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    // MÉTODO PARA OBTENER NOTIFICACIONES PENDIENTES
    // Busca todas las notificaciones por el ID del usuario y donde 'seen' sea el valor proporcionado (0 para no vistas)




    List<NotificationEntity> findByUserIdOrderByNotificationDateDesc(Long userId);
    void deleteByUserId(Long userId);
}
