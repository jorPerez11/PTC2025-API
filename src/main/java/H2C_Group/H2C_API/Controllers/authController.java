package H2C_Group.H2C_API.Controllers;

import H2C_Group.H2C_API.Models.DTO.ChangePasswordDTO;
import H2C_Group.H2C_API.Models.DTO.UserDTO;
import H2C_Group.H2C_API.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class authController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserDTO userDTO){
        try {
            UserDTO registeredUser = userService.registerNewUser(userDTO);
            return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordDTO changePasswordDTO){
        try {
            UserDTO updatedUser = userService.changePassword(
                    changePasswordDTO.getUsername(),
                    changePasswordDTO.getCurrentPassword(),
                    changePasswordDTO.getNewPassword()
            );
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
