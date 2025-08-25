package H2C_Group.H2C_API.Controllers;


import H2C_Group.H2C_API.Entities.UserEntity;
import H2C_Group.H2C_API.Exceptions.ExceptionUserNotFound;
import H2C_Group.H2C_API.Models.DTO.UserDTO;
import H2C_Group.H2C_API.Services.UserService;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
    public ResponseEntity<Page<UserDTO>> GetUserData(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<UserDTO> userPage = acceso.findAll(page, size);
        return ResponseEntity.ok(userPage);
    }

    //metodo para obtener usuarios con rol de tecnico
    @GetMapping("/users/tech")
    public ResponseEntity<?> getTechUsers(
            @RequestParam(value = "roleId", required = false)Long roleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            //Parametros de filtracion
            @RequestParam(defaultValue = "") String term,
            @RequestParam(defaultValue = "all") String category,
            @RequestParam(defaultValue = "all") String period)
    {

        try{
            Page<UserDTO> techUsers = acceso.getFilteredTechUsers(page, size, term, category, period);
            return ResponseEntity.ok(techUsers);
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener los técnicos");
        }
    }

        @PostMapping("/PostUser")
    public ResponseEntity<?> createUser(@RequestBody UserDTO user) {
        System.out.println("DEBUG: Entrando al metodo createUser en el controlador.");
        try{
            UserDTO newUser = acceso.registerNewUser(user);
            return new ResponseEntity<>(newUser, HttpStatus.CREATED);
        }catch (IllegalArgumentException e) {
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
