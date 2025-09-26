package H2C_Group.H2C_API.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void sendPrivateNotification(String userId, String message) {
        // 'convertAndSendToUser' se encarga de dirigir el mensaje a la sesión del usuario
        // El destino final del cliente será /user/queue/notifications
        messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", message);
    }
}