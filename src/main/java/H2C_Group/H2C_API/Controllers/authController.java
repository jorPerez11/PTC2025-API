package H2C_Group.H2C_API.Controllers;

import H2C_Group.H2C_API.Entities.UserEntity;
import H2C_Group.H2C_API.Exceptions.ExceptionUserNotFound;
import H2C_Group.H2C_API.Models.DTO.ChangePasswordDTO;
import H2C_Group.H2C_API.Models.DTO.UserDTO;
import H2C_Group.H2C_API.Repositories.UserRepository;
import H2C_Group.H2C_API.Services.UserService;
import H2C_Group.H2C_API.Utils.JwUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;


@Slf4j
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

    @PostMapping("/registerTech")
    public ResponseEntity<?> registerTech(@RequestBody UserDTO userDTO){
        try{
            UserDTO registeredUserTech = userService.registerNewUserTech(userDTO);
            return new ResponseEntity<>(registeredUserTech, HttpStatus.CREATED);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO authenticationRequest, HttpServletResponse httpResponse) throws Exception{
        try {
            // 1. Autentica las credenciales del usuario usando AuthenticationManager
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword()));

            //2. Si la autenticacion fue exitosa, llama al metodo auxiliar para agregar la cookie
            addTokenCookie(authenticationRequest.getUsername(), httpResponse);

            //3. Retornamos los datos del /authme
            return ResponseEntity.ok().body(Map.of(
                    "message", "Login exitoso",
                    "status", "success"
            ));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "error", "Login fallido",
                            "message", "Credenciales invalidas"
                    ));
        }

    }

    private void addTokenCookie(String username, HttpServletResponse httpServletResponse) throws Exception{
        //1. Carga los datos del usuario
        final UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        //2. Obtiene el estado de la contraseña expirada del usuario
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(()-> new ExceptionUserNotFound("Usuario no encontrado despues de la autenticacion"));
        boolean passwordExpired = userEntity.isPasswordExpired();

        //3. General el token de larga o corta duracion
        long expirationTime = passwordExpired ? (long) jwtUtil.JWT_TOKEN_VALIDITY_SHORT : jwtUtil.JWT_TOKEN_VALIDITY_LONG;


        String jwt = jwtUtil.generateToken(userDetails, expirationTime);

        Cookie cookie = new Cookie("authToken", jwt);

        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge((int) (expirationTime / 1000));
        cookie.setAttribute("SameSite", "Lax");
        cookie.setSecure(false);
        httpServletResponse.addCookie(cookie);
        System.out.println("Cookie 'authToken' creada y agregada con SameSite=Lax.");


//        //4. Construye la cadena de la cookie y la añade a la respuesta
//        String coockieValue = String.format(
//                "authToken=%s; " +
//                        "Path=/; " +
//                        "HttpOnly; " +
//                        "Secure=false; " +
//                        "SameSite=None; " +
//                        "MaxAge=%d; " ,
//                jwt,
//                expirationTime / 1000
//        );
//
//        System.out.println("Cookie creada"+ coockieValue);
//        httpServletResponse.addHeader("Set-Cookie", coockieValue);
//        httpServletResponse.addHeader("Access-Control-Expose-Headers", "Set-Cookie");
//        System.out.println("Headers añadidos a la respuesta");
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

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse httpServletResponse){
        //Elimina la cookie del navegador
        String cookieValue = "authToken=; Path=/; HttpOnly; Secure; SameSite=None; MaxAge=0";
        httpServletResponse.addHeader("Set-Cookie", cookieValue);
        httpServletResponse.addHeader("Access-Control-Expose-Headers", "Set-Cookie");

        return ResponseEntity.ok().body("Sesion cerrada con exito");
    }

    @PostMapping("/authme")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        try {
            //1. Extraer el token de la cookie
            String token = extractTokenFromCookie(request);

            if (token == null){
                log.warn("No se encontro ningun token en las cookies");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "error", "no autenticado",
                                "message", "Token no encontrado"
                        ));
            }

            //2. Validar el token
            if (!jwtUtil.validateToken(token)){
                log.warn("token invalido o expirado");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "error", "No autenticado",
                                "message", "Token invalido o expirado"
                        ));
            }

            //3. Extraer todos los datos del token

            String username = jwtUtil.getUsernameFromToken(token);
            String rol = jwtUtil.extractRol(token);
            Long userId = jwtUtil.getUserIdFromToken(token);
            Boolean passwordExpired = jwtUtil.getIsPasswordExpiredFromToken(token);

            log.info("Usuario autenticado: {} (ID: {}, Rol: {})" , username, userId, rol);

            //4. Crear respuesta con datos del token
            Map<String, Object> response = new HashMap<>();
            response.put("username", username);
            response.put("rol", rol);
            response.put("passwordExpired", passwordExpired);
            response.put("userId", userId);

            return ResponseEntity.ok(response);

        } catch (Exception e){
            log.error("Error al autenticarse en /authme:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error del servidor",
                            "message", e.getMessage()
                    ));
        }
    }

    private String extractTokenFromCookie(HttpServletRequest request){
        try {
            if (request.getCookies() != null ){
                for (Cookie cookie: request.getCookies()){
                    if ("authToken".equals(cookie.getName())){
                        log.debug("Cookie authToken encontrada");
                        return cookie.getValue();
                    }
                }
            }
            log.debug("No se encontraron cookies");
            return null;

        } catch (Exception e){
            log.error("Error al extraer token de cookie: " , e);
            return null;
        }

    }
}
