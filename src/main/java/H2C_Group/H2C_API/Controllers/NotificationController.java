package H2C_Group.H2C_API.Controllers;

import H2C_Group.H2C_API.Exceptions.ExceptionNotificationBadRequest;
import H2C_Group.H2C_API.Exceptions.ExceptionNotificationNotFound;
import H2C_Group.H2C_API.Models.DTO.NotificationDTO;
import H2C_Group.H2C_API.Services.NotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.management.Notification;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class NotificationController {
    @Autowired
    private NotificationService acceso;

    @GetMapping("/GetNotifications")
    public ResponseEntity<Page<NotificationDTO>> getAllNotifications(
            @PageableDefault(page = 0, size = 10)
            Pageable pageable) {
        Page<NotificationDTO> notifications = acceso.findAll(pageable);
        return new ResponseEntity<>(notifications, HttpStatus.OK);
    }

    @PostMapping("/PostNotification")
    public ResponseEntity<?> postNotification(@RequestBody @Valid NotificationDTO notificationDTO) {
        try{
            NotificationDTO newNotification = acceso.createNotification(notificationDTO);
            return new ResponseEntity<>(newNotification, HttpStatus.CREATED);
        }catch (ExceptionNotificationBadRequest e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST); // Código 400
        }catch (ExceptionNotificationNotFound e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND); // Código 404
        }catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", "Ocurrió un error interno del servidor al crear la notificacion.");
            return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR); // Código 500
        }
    }


    @PatchMapping("/UpdateNotificationState/{id}")
    public ResponseEntity<?> updateNotificationState(@PathVariable Long id, @RequestBody @Valid NotificationDTO notificationDTO) {
        try{
            NotificationDTO newNotification = acceso.updateNotificationState(id, notificationDTO);
            return new ResponseEntity<>(newNotification, HttpStatus.OK);
        }catch (ExceptionNotificationBadRequest e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST); // Código 400
        }catch (ExceptionNotificationNotFound e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND); // Código 404
        }catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", "Ocurrió un error interno del servidor al crear la notificacion.");
            return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR); // Código 500
        }
    }




    @DeleteMapping("/DeleteNotification/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        try{
            acceso.deleteNotification(id);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (ExceptionNotificationBadRequest e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors.toString(), HttpStatus.BAD_REQUEST);
        }catch (ExceptionNotificationNotFound e){
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors.toString(), HttpStatus.NOT_FOUND);
        }catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", "Ocurrió un error interno del servidor al intentar eliminar la notificacion.");
            return new ResponseEntity<>(errors.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
