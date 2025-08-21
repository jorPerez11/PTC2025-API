package H2C_Group.H2C_API.Controllers;


import H2C_Group.H2C_API.Exceptions.ExceptionUserBadRequest;
import H2C_Group.H2C_API.Exceptions.ExceptionUserNotFound;
import H2C_Group.H2C_API.Models.DTO.UserDTO;
import H2C_Group.H2C_API.Services.UserService;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService acceso;

    @GetMapping("/GetUsers")
    public ResponseEntity<Page<UserDTO>> getUsers(
            @PageableDefault(page = 0, size = 10)
            Pageable pageable) {
        Page<UserDTO> users = acceso.findAll(pageable);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PostMapping("/PostUser")
    public ResponseEntity<?> createUser(@RequestBody @Valid UserDTO user) {
        System.out.println("DEBUG: Entrando al metodo createUser en el controlador.");
        try{
            UserDTO newUser = acceso.registerNewUser(user);
            return new ResponseEntity<>(newUser, HttpStatus.CREATED);
        }catch (ExceptionUserBadRequest e) {
            //Validación de argumentos invalidos
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST); // Código 400
        }catch (ExceptionUserNotFound e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND); // Código 404
        }catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            e.printStackTrace();
            errors.put("error", "Ocurrió un error interno del servidor al crear el usuario.");
            return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR); // Código 500
        }
    }

    @PatchMapping("/UpdateUser/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO dto) {
        try {
            UserDTO updatedUser = acceso.updateUser(id, dto);
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);

        } catch (ExceptionUserBadRequest e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }catch(ExceptionUserNotFound e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND);
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
        }catch(ExceptionUserBadRequest e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }catch(ExceptionUserNotFound e) {
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
