package H2C_Group.H2C_API.Controllers;

import H2C_Group.H2C_API.Entities.NotificationEntity;
import H2C_Group.H2C_API.Exceptions.ExceptionUserNotFound;
import H2C_Group.H2C_API.Repositories.NotificationRepository;
import H2C_Group.H2C_API.Repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/notifications")
public class NotificationRestController {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Endpoint para obtener todas las notificaciones no vistas de un usuario.
     * El cliente debe llamar a esto al iniciar sesión.
     */
    @GetMapping("/pending/{userId}")
    @PreAuthorize("isAuthenticated()")
    public List<NotificationEntity> getPendingNotifications(@PathVariable Long userId) {
        System.out.println("✅ INICIO DE RUTA NOTIFICACIONES. USER ID: " + userId);

        // Usamos el método que definiste en el NotificationRepository (seen = 0)
        return notificationRepository.findByUser_UserIdAndSeenOrderByNotificationDateDesc(userId, 0);
    }

    /**
     * Endpoint para marcar una notificación específica como vista (seen = 1).
     */
    @PutMapping("/mark-as-seen/{notificationId}")
    @Transactional
    public ResponseEntity<?> markNotificationAsSeen(@PathVariable Long notificationId) {
        Optional<NotificationEntity> optionalNotification = notificationRepository.findById(notificationId);

        if (optionalNotification.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        NotificationEntity notification = optionalNotification.get();

        // Solo actualizamos si no ha sido vista ya
        if (notification.getSeen() == 0) {
            notification.setSeen(1); // 1 = Visto
            notificationRepository.save(notification);
        }

        // Retornamos 200 OK sin contenido
        return ResponseEntity.ok().build();
    }

    /**
     * Opcional: Obtener un conteo de notificaciones no vistas.
     */
    @GetMapping("/count/pending/{userId}")
    public Long countPendingNotifications(@PathVariable Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ExceptionUserNotFound("El ID de usuario " + userId + " no existe.");
        }

        return notificationRepository.countByUser_UserIdAndSeen(userId, 0);
    }
}
