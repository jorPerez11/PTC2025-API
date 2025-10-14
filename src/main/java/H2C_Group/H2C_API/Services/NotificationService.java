package H2C_Group.H2C_API.Services;

import H2C_Group.H2C_API.Entities.NotificationEntity;
import H2C_Group.H2C_API.Entities.TicketEntity;
import H2C_Group.H2C_API.Entities.UserEntity;
import H2C_Group.H2C_API.Repositories.NotificationRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private NotificationRepository notificationRepository;

    @Transactional
    public void createAndSendNotification(UserEntity user, TicketEntity ticket, String message) {
        // 1. Guardar en la BD
        NotificationEntity notification = new NotificationEntity();
        notification.setUser(user);
        notification.setTicket(ticket);
        notification.setMessage(message);
        notification.setSeen(0); // 0 = No Visto (basado en tu esquema de DB)

        NotificationEntity savedNotification = notificationRepository.save(notification);

        // 2. Enviar por WebSocket
        // Usamos el 'username' del UserEntity como identificador del Principal
        String userId = user.getUsername();

        // Creamos un payload JSON simple (esto es lo que el frontend recibirá)
        String payload = String.format("{\"id\": %d, \"message\": \"%s\", \"ticketId\": %d, \"date\": \"%s\"}",
                savedNotification.getNotificationId(),
                message,
                ticket.getTicketId(),
                savedNotification.getNotificationDate().toString());

        sendPrivateNotification(userId, payload);
    }

    public void sendPrivateNotification(String username, String payload) {
        // 'convertAndSendToUser' se encarga de dirigir el mensaje a la sesión del usuario
        // El destino final del cliente será /user/queue/notifications
        System.out.println("📤 WS enviado a: " + username);
        messagingTemplate.convertAndSendToUser(username, "/queue/notifications", payload);
    }
}