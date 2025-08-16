package H2C_Group.H2C_API.Controllers;

import H2C_Group.H2C_API.Entities.UserEntity;
import H2C_Group.H2C_API.Models.DTO.ChangePasswordDTO;
import H2C_Group.H2C_API.Models.DTO.UserDTO;
import H2C_Group.H2C_API.Repositories.UserRepository;
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

import java.io.Serializable;

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

    @Autowired
    private UserRepository userRepository;

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
        UserEntity userEntity = userRepository.findByUsername(authenticationRequest.getUsername())
                .orElseThrow(() -> new Exception ("Usuario no encontrado después de la autenticación"));

        // 3. Obtenemos el estado de la contraseña expirada del usuario
        boolean passwordExpired = userEntity.isPasswordExpired();

        //4. genera el token de larga o corta duracion
        String jwt;

        //Si la contraseña no esta expirada genera el token de larga duracion
        if (passwordExpired){
            //Si la contraseña es de corta duracion genera un token de corta duracion
            jwt = jwtUtil.generateToken(userEntity, JwUtil.JWT_TOKEN_VALIDITY_SHORT);
        } else {
            //Si la contraseña es de larga duracion genera un token de larga duracion
            jwt = jwtUtil.generateToken(userEntity, JwUtil.JWT_TOKEN_VALIDITY_LONG);
        }

        // 5.. Retorna el token en la respuesta
        LoginResponse response = new LoginResponse(jwt, userEntity.getUsername(), userEntity.getRolId(), passwordExpired);
        return ResponseEntity.ok(response);
    }

    // DTO para la respuesta del login
    static class LoginResponse implements Serializable {
        private String token;
        private String username;
        private Long rolId;
        private boolean passwordExpired;

        public LoginResponse(String token, String username, Long rolId, boolean passwordExpired){
            this.token = token;
            this.username = username;
            this.rolId = rolId;
            this.passwordExpired = passwordExpired;
        }
        public String getToken(){return token;}
        public void setToken(String token){ this.token = token;}
        public String getUsername(){return username;}
        public void setUsername(String username){ this.username = username;}
        public Long getRolId(){return rolId;}
        public void setRolId(Long rolId){ this.rolId = rolId;}
        public boolean isPasswordExpired(){return passwordExpired;}
        public void setPasswordExpired(boolean passwordExpired){ this.passwordExpired = passwordExpired;}
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
