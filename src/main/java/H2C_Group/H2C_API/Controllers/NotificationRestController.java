package H2C_Group.H2C_API.Controllers;

import H2C_Group.H2C_API.Services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequestMapping("/api/notifications")
public class NotificationRestController {
    @Autowired
    private NotificationService notificationService;

    // El frontend enviará: GET /api/notifications/history?userId=123
    @GetMapping("/history")
    public ResponseEntity<List<?>> getNotificationHistory(
            @RequestParam(name = "userId") Long currentUserId // El ID viene como parámetro de consulta
    ) {

        List<?> history = notificationService.getNotificationsByUserId(currentUserId);

        return ResponseEntity.ok(history);
    }
}
