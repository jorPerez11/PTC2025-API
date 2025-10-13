package H2C_Group.H2C_API.Services;

import H2C_Group.H2C_API.Entities.NotificationEntity;
import H2C_Group.H2C_API.Entities.TicketEntity;
import H2C_Group.H2C_API.Entities.UserEntity;
import H2C_Group.H2C_API.Models.DTO.NotificationMessageDTO;
import H2C_Group.H2C_API.Repositories.NotificationRepository;
import H2C_Group.H2C_API.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private NotificationRepository notificationRepository;

    /**
     * Guarda la notificación en la base de datos y la envía por WebSocket.
     * @param userIdToNotify El ID del usuario destino.
     * @param ticket La entidad del Ticket que causó la notificación (puede ser null).
     * @param message El texto de la notificación.
     * @param type El tipo de notificación (Ej: "ASSIGNMENT", "REMINDER").
     */
    public void sendNotification(Long userIdToNotify, TicketEntity ticket, String message, String type) {
        try {
            // 1. Obtener la entidad del usuario (para la FK en TBNOTIFICATIONS)
            UserEntity user = userRepository.findById(userIdToNotify)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + userIdToNotify));

            // 2. Crear y persistir la notificación en la base de datos
            NotificationEntity newNotification = new NotificationEntity();
            newNotification.setUser(user);
            newNotification.setTicket(ticket);
            newNotification.setMessage(message);
            newNotification.setSeen(0);
            newNotification.setNotificationDate(LocalDateTime.now());

            NotificationEntity savedNotification = notificationRepository.save(newNotification);

            // 3. Preparar el DTO para el envío por WebSocket
            NotificationMessageDTO notificationDTO = new NotificationMessageDTO(
                    savedNotification.getNotificationId(),
                    ticket != null ? ticket.getTicketId() : null,
                    message,
                    type
            );

            // 4. Enviar la notificación privada: /user/{userId}/queue/notifications
            // Esto usa el ID del usuario para el enrutamiento.
            messagingTemplate.convertAndSendToUser(
                    userIdToNotify.toString(),
                    "/queue/notifications",
                    notificationDTO
            );
        } catch (Exception e) {
            System.err.println("❌ Error al enviar/guardar notificación para el usuario " + userIdToNotify + ": " + e.getMessage());
        }
    }
}