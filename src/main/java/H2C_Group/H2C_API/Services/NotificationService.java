package H2C_Group.H2C_API.Services;

import H2C_Group.H2C_API.Entities.NotificationEntity;
import H2C_Group.H2C_API.Entities.UserEntity;
import H2C_Group.H2C_API.Repositories.NotificationRepository;
import H2C_Group.H2C_API.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.management.Notification;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class NotificationService {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Inyectamos el Repository para acceder a la base de datos
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    public void sendPrivateNotification(String userId, String message) {
        // 'convertAndSendToUser' se encarga de dirigir el mensaje a la sesión del usuario
        // El destino final del cliente será /user/queue/notifications
        messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", message);
    }

    /**
     * Recupera el historial de notificaciones para un usuario específico.
     * @param userId El ID del usuario actual (enviado desde el frontend).
     * @return Una lista de NotificationEntity.
     */
    public List<NotificationEntity> getNotificationsByUserId(Long userId) {

        // 1. Buscar la Entidad UserEntity utilizando el userId.
        // Se usa .orElseThrow para asegurar que la notificación es para un usuario existente.
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("No se encontró el usuario con ID: " + userId));

        // 2. Usar el objeto UserEntity para consultar las notificaciones.
        return notificationRepository.findByUserOrderByNotificationDateDesc(user);
    }
}