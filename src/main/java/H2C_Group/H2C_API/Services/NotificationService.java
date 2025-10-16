package H2C_Group.H2C_API.Services;

import H2C_Group.H2C_API.Entities.NotificationEntity;
import H2C_Group.H2C_API.Entities.TicketEntity;
import H2C_Group.H2C_API.Entities.UserEntity;
import H2C_Group.H2C_API.Repositories.NotificationRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository repository;

    public void crear(NotificationEntity noti) {
        repository.save(noti);
    }

    public List<NotificationEntity> obtenerPorUsuario(Long userId) {
        return repository.findByUserIdOrderByNotificationDateDesc(userId);
    }

    public void borrarTodas(Long userId) {
        repository.deleteByUserId(userId);
    }

    public NotificationEntity obtenerPorId(Long id) {
        return repository.findById(id).orElse(null);
    }

}