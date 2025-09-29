package H2C_Group.H2C_API.Controllers;


import H2C_Group.H2C_API.Entities.UserEntity;
import H2C_Group.H2C_API.Exceptions.ExceptionUserBadRequest;
import H2C_Group.H2C_API.Exceptions.ExceptionUserNotFound;
import H2C_Group.H2C_API.Models.DTO.AllUsersDTO;
import H2C_Group.H2C_API.Models.DTO.ProfileDTO;
import H2C_Group.H2C_API.Models.DTO.UserDTO;
import H2C_Group.H2C_API.Services.CloudinaryService;
import H2C_Group.H2C_API.Services.UserService;
import jakarta.validation.Valid;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService acceso;

    @Autowired
    private CloudinaryService cloudinaryService;

//    @GetMapping("/GetUsers")
//    public ResponseEntity<Page<UserDTO>> GetUserData(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size
//    ) {
//        Page<UserDTO> userPage = acceso.findAll(page, size);
//        return ResponseEntity.ok(userPage);
//    }

    @GetMapping("/GetTicketDetailsForModal/{ticketId}")
    public ResponseEntity<?> getTicketDetailsForModal(@PathVariable Long ticketId) {
        try {
            AllUsersDTO ticketDetails = acceso.getTicketDetailsForModal(ticketId);
            return new ResponseEntity<>(ticketDetails, HttpStatus.OK);
        } catch (ExceptionUserNotFound e) { // Usar ExceptionUserNotFound si así la maneja el servicio
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND); // Código 404
        } catch (Exception e) {
            return new ResponseEntity<>("Error interno del servidor al obtener los detalles del ticket.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // --- Endpoint para obtener usuarios con paginación y filtros (/GetUsers) ---
    @GetMapping("/GetUsers")
    public ResponseEntity<Page<UserDTO>> GetUserData(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String term, // Nuevo: Término de búsqueda
            @RequestParam(defaultValue = "all") String category, // Nuevo: Filtro por categoría/estado
            @RequestParam(defaultValue = "all") String period // Nuevo: Filtro por período
    ) {
        if(size <= 0 || size > 50){
            return ResponseEntity.badRequest().body(Page.empty(PageRequest.of(page, size))); // Retorna Page vacía
        }
        Page<UserDTO> userPage = acceso.findAll(page, size, term, category, period); // Pasa los nuevos parámetros
        if (userPage == null || userPage.isEmpty()) {
            return ResponseEntity.noContent().build(); // Devuelve 204 No Content si no hay usuarios
        }
        return ResponseEntity.ok(userPage);
    }


    @GetMapping("/GetTech")
    public ResponseEntity<List<UserDTO>> getTechs(){
        List<UserDTO> tecnicos = acceso.getTech();
        if (tecnicos.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(tecnicos);
    }


    // Método de actualización único y correcto, usa UserDTO y valida con @Valid.
    @PatchMapping("/users/{id}")
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
    public ResponseEntity<?> createUser(@RequestBody @Valid UserDTO user) {
        try {
            UserDTO newUser = acceso.registerNewUser(user);
            // Devuelve el objeto de usuario creado
            return new ResponseEntity<>(newUser, HttpStatus.CREATED);
        } catch (ExceptionUserBadRequest e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST); // Código 400
        } catch (ExceptionUserNotFound e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND); // Código 404
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", "Ocurrió un error interno del servidor al crear el usuario.");
            return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/UpdateUser/{id}")
    public ResponseEntity<?> UpdateUser(@PathVariable Long id, @Valid @RequestBody UserDTO dto) {
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

    //Obtener un usuario por su ID
    @GetMapping("/GetUser/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            UserDTO user = acceso.findUserById(id); // Llamaremos a un nuevo método en el servicio
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (ExceptionUserNotFound e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND); // Código 404
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            e.printStackTrace();
            errors.put("error", "Ocurrió un error interno del servidor al buscar el usuario.");
            return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR); // Código 500

        }
    }

    @GetMapping("/GetUserByUsername/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        System.out.println("Entrando al endpoint GetUserIdByUsername para: {}" + username); // Log 1);
        try {
            UserDTO user = acceso.findUserByUsername(username);
            System.out.println("ID de usuario obtenido con éxito: {}" + user);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (ExceptionUserNotFound e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            System.out.println("Error al obtener ID de usuario: " + e);
            errors.put("error", "Ocurrió un error interno del servidor al buscar el usuario por nombre de usuario.");
            return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint para actualizar solo la foto de perfil del usuario.
     * @param id El ID del usuario.
     * @param file El archivo de la nueva foto de perfil.
     * @return El objeto de usuario actualizado.
     */
    @PostMapping(path = "/users/{id}/profile-picture", consumes = "multipart/form-data")
    public ResponseEntity<?> updateUserProfilePicture(
            @PathVariable Long id,
            @RequestParam("profilePicture") MultipartFile file) {
        try {
            // Subir la imagen a Cloudinary
            String imageUrl = cloudinaryService.uploadImage(file, "profile_pictures");

            // Llamar a tu servicio para actualizar solo la URL de la imagen en la base de datos.
            // Necesitarás un método como este en tu clase de servicio.
            UserDTO updatedUser = acceso.updateUserProfilePicture(id, imageUrl);

            return new ResponseEntity<>(updatedUser, HttpStatus.OK);

        } catch (ExceptionUserNotFound e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", "Error al subir la imagen: " + e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", "Ocurrió un error interno del servidor al actualizar la imagen.");
            return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint para actualizar el perfil completo del usuario, incluyendo
     * la foto de perfil.
     * Nota: Utiliza un método POST con 'multipart/form-data' para
     * poder manejar tanto archivos como datos de texto en una sola petición.
     */
    @PostMapping(path = "/users/{id}/profile", consumes = "multipart/form-data")
    public ResponseEntity<?> updateUserProfile(
            @PathVariable Long id,
            @RequestParam(value = "profilePicture", required = false) MultipartFile file,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "phone", required = false) String phone) {

        try {
            System.out.println("🔄 Actualizando perfil para usuario ID: " + id);
            System.out.println("📊 Datos recibidos - Name: " + name + ", Email: " + email + ", Phone: " + phone);
            System.out.println("📸 Archivo recibido: " + (file != null ? file.getOriginalFilename() + " (" + file.getSize() + " bytes)" : "null"));

            // 1. Obtén el usuario existente
            UserDTO existingUser = acceso.findUserById(id);

            // 2. Actualiza solo los campos proporcionados
            if (name != null && !name.trim().isEmpty()) {
                existingUser.setName(name.trim());
            }
            if (email != null && !email.trim().isEmpty()) {
                existingUser.setEmail(email.trim());
            }
            if (phone != null && !phone.trim().isEmpty()) {
                existingUser.setPhone(phone.trim());
            }

            // 3. Sube la imagen si se proporciona
            if (file != null && !file.isEmpty()) {
                System.out.println("🖼️ Subiendo imagen a Cloudinary...");
                String imageUrl = cloudinaryService.uploadImage(file);
                System.out.println("✅ Imagen subida, URL: " + imageUrl);
                existingUser.setProfilePictureUrl(imageUrl);
            } else {
                System.out.println("⚠️ No se recibió archivo de imagen");
            }

            // 4. Usa el nuevo método específico para perfil
            UserDTO updatedUser = acceso.updateUserProfile(id, existingUser);
            System.out.println("✅ Perfil actualizado exitosamente");

            return new ResponseEntity<>(updatedUser, HttpStatus.OK);

        } catch (Exception e) {
            System.out.println("❌ Error en updateUserProfile: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/users/{id}")
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

    @GetMapping("/users/counts-by-month")
    public ResponseEntity<Map<String, Integer>> getNewUsersCountsByMonth() {
        // Llama a tu servicio para obtener el mapa
        Map<String, Integer> data = acceso.getNewUsersCountsMap();

        return ResponseEntity.ok(data);
    }
}