package H2C_Group.H2C_API.Controllers;

import H2C_Group.H2C_API.Entities.NotificationEntity;
import H2C_Group.H2C_API.Services.NotificationService;
import org.apache.logging.log4j.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificationController {
    @Autowired
    private NotificationService service;

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody NotificationEntity noti) {
        service.crear(noti);
        return ResponseEntity.ok("Notificación creada");
    }

    @GetMapping
    public ResponseEntity<List<NotificationEntity>> obtener(@RequestParam Long usuarioId) {
        return ResponseEntity.ok(service.obtenerPorUsuario(usuarioId));
    }

    @DeleteMapping
    public ResponseEntity<?> borrar(@RequestParam Long usuarioId) {
        service.borrarTodas(usuarioId);
        return ResponseEntity.ok("Notificaciones eliminadas");
    }

    @PatchMapping("/{id}/marcarVista")
    public ResponseEntity<?> marcarComoVista(@PathVariable Long id) {
        NotificationEntity noti = service.obtenerPorId(id);
        if (noti == null) {
            return ResponseEntity.notFound().build();
        }

        noti.setSeen(1); // ✅ marcar como vista
        service.crear(noti); // reutilizás el método que guarda

        return ResponseEntity.ok("Notificación marcada como vista");
    }

}
