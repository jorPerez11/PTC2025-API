package H2C_Group.H2C_API.Controllers;

import H2C_Group.H2C_API.Models.DTO.ChangePasswordDTO;
import H2C_Group.H2C_API.Models.DTO.UserDTO;
import H2C_Group.H2C_API.Services.UserService;
import H2C_Group.H2C_API.Utils.JwUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/users")
public class authController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserDTO userDTO){
        try {
            UserDTO registeredUser = userService.registerNewUser(userDTO);
            return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody UserDTO authenticationRequest) throws Exception{
        // 1. Autentica las credenciales del usuario usando AuthenticationManager
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword()));

        // 2. Si la autenticación fue exitosa, carga los detalles del usuario
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());

        // 3. Genera un token JWT usando la clase JwtUtil
        final String jwt = jwtUtil.generateToken(userDetails);

        // 4. Retorna el token en la respuesta
        return ResponseEntity.ok(new AuthenticationResponse(jwt));
    }

    // DTO para la respuesta del login
    static class AuthenticationResponse {
        private final String token;
        public AuthenticationResponse(String token) {
            this.token = token;
        }
        public String getToken() {
            return token;
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
