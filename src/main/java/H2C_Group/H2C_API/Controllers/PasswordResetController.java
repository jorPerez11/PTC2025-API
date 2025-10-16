package H2C_Group.H2C_API.Controllers;

import H2C_Group.H2C_API.Entities.NotificationEntity;
import H2C_Group.H2C_API.Entities.UserEntity;
import H2C_Group.H2C_API.Repositories.UserRepository;
import H2C_Group.H2C_API.Services.NotificationService;
import H2C_Group.H2C_API.Services.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;


    @PostMapping("/request")
    public ResponseEntity<?> requestReset(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        passwordResetService.requestReset(email);
        return ResponseEntity.ok("Si el correo está registrado, se enviará un código.");
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String token = payload.get("token");

        boolean valid = passwordResetService.verifyToken(email, token);
        if (!valid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Código inválido o expirado.");
        }

        return ResponseEntity.ok("Código válido.");
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmReset(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String token = payload.get("token");
        String newPassword = payload.get("newPassword");

        try {
            passwordResetService.resetPassword(email, token, newPassword);

            UserEntity user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("No se encontró el usuario con ese correo."));

            NotificationEntity noti = new NotificationEntity();
            noti.setUserId(user.getUserId());
            noti.setMessage("Tu contraseña ha sido actualizada correctamente.");
            notificationService.crear(noti);

            return ResponseEntity.ok("Contraseña actualizada.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }

    }
}
