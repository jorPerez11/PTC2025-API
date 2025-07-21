package H2C_Group.H2C_API.Controllers;


import H2C_Group.H2C_API.Exceptions.UserExceptions;
import H2C_Group.H2C_API.Models.DTO.UserDTO;
import H2C_Group.H2C_API.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService acceso;

    @GetMapping("/GetUser")
    public List<UserDTO> GetUserData() {
        return acceso.getAllUsers();
    }

    @PostMapping("/PostUser")
    public ResponseEntity<?> createUser(@RequestBody UserDTO user) {
        try{
            UserDTO newUser = acceso.registerNewUser(user);
            return new ResponseEntity<>(newUser, HttpStatus.CREATED);
        }catch (IllegalArgumentException e) {
            //Validación de argumentos invalidos
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST); // Código 400
        }catch (UserExceptions.UserNotFoundException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND); // Código 404
        }catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", "Ocurrió un error interno del servidor al crear el usuario.");
            return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR); // Código 500
        }
    }

    @PatchMapping("/UpdateUser/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDTO dto) {
        try {
            UserDTO updatedUser = acceso.updateUser(id, dto);
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            //e.printStackTrace();
            errors.put("error", "Ocurrió un error interno del servidor al actualizar el usuario.");
            return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/DeleteUser/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            acceso.deleteUser(id);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch(IllegalArgumentException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }catch(UserExceptions.UserNotFoundException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND);
        }catch(Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", "Ocurrió un error interno del servidor al eliminar el usuario.");
            return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
