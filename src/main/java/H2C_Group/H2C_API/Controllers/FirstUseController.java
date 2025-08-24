package H2C_Group.H2C_API.Controllers;

import H2C_Group.H2C_API.Entities.CategoryEntity;
import H2C_Group.H2C_API.Exceptions.ExceptionCategoryBadRequest;
import H2C_Group.H2C_API.Exceptions.ExceptionCategoryNotFound;
import H2C_Group.H2C_API.Models.DTO.RolDTO;
import H2C_Group.H2C_API.Models.DTO.UserDTO;
import H2C_Group.H2C_API.Repositories.CategoryRepository;
import H2C_Group.H2C_API.Services.CategoryService;
import H2C_Group.H2C_API.Services.UserService;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/firstuse")
public class FirstUseController {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryService categoryService;

    // Endpoint para obtener técnicos (rolId = 2 para técnicos)
    @GetMapping("/tecnicoData")
    public ResponseEntity<List<Map<String, Object>>> getTecnicos() {
        try {
            List<UserDTO> technicians = userService.findByRole(2L);

            List<Map<String, Object>> response = technicians.stream().map(user -> {
                Map<String, Object> tech = new HashMap<>();
                tech.put("id", user.getId());
                tech.put("Nombre", user.getName());
                tech.put("Correo Electrónico", user.getEmail());
                tech.put("Número de tel.", user.getPhone());
                tech.put("Foto", user.getProfilePictureUrl());
                return tech;
            }).collect(Collectors.toList());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint para crear técnico
    @PostMapping("/tecnicoData")
    @PermitAll
    public ResponseEntity<Map<String, Object>> createTecnico(@RequestBody Map<String, Object> payload) {
        try {
            String nombre = (String) payload.get("Nombre");
            String email = (String) payload.get("Correo Electrónico");
            String telefono = (String) payload.get("Número de tel.");
            String foto = (String) payload.get("Foto");

            UserDTO userDTO = new UserDTO();
            userDTO.setName(nombre);
            userDTO.setEmail(email);
            userDTO.setPhone(telefono);
            userDTO.setProfilePictureUrl(foto);

            // Configurar el rol como técnico (ID = 2)
            RolDTO rolDTO = new RolDTO();
            rolDTO.setId(2L);
            rolDTO.setDisplayName("Técnico");
            userDTO.setRol(rolDTO);

            userDTO.setUsername(generateUsername(nombre));
            userDTO.setPassword("TempPassword123!");
            userDTO.setIsActive(1);

            UserDTO savedUser = userService.registerNewUser(userDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("id", savedUser.getId());
            response.put("Nombre", savedUser.getName());
            response.put("Correo Electrónico", savedUser.getEmail());
            response.put("Número de tel.", savedUser.getPhone());
            response.put("Foto", savedUser.getProfilePictureUrl());

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint para actualizar técnico
    @PutMapping("/tecnicoData/{id}")
    public ResponseEntity<Map<String, Object>> updateTecnico(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        try {
            String nombre = (String) payload.get("Nombre");
            String email = (String) payload.get("Correo Electrónico");
            String telefono = (String) payload.get("Número de tel.");
            String foto = (String) payload.get("Foto");

            UserDTO userDTO = new UserDTO();
            userDTO.setName(nombre);
            userDTO.setEmail(email);
            userDTO.setPhone(telefono);
            userDTO.setProfilePictureUrl(foto);

            UserDTO updatedUser = userService.updateUser(id, userDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("id", updatedUser.getId());
            response.put("Nombre", updatedUser.getName());
            response.put("Correo Electrónico", updatedUser.getEmail());
            response.put("Número de tel.", updatedUser.getPhone());
            response.put("Foto", updatedUser.getProfilePictureUrl());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint para eliminar técnico
    @DeleteMapping("/tecnicoData/{id}")
    public ResponseEntity<Void> deleteTecnico(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint para obtener categorías
    @GetMapping("/categorias")
    public ResponseEntity<List<Map<String, Object>>> getCategorias() {
        try {
            List<CategoryEntity> categories = categoryRepository.findAll();

            List<Map<String, Object>> categorias = categories.stream()
                    .map(category -> {
                        Map<String, Object> catMap = new HashMap<>();
                        catMap.put("id", category.getCategoryId());
                        catMap.put("nombreDepartamento", category.getCategoryName());
                        return catMap;
                    })
                    .collect(Collectors.toList());

            return new ResponseEntity<>(categorias, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint para crear categoría
    @PostMapping("/categorias")
    @PermitAll
    public ResponseEntity<Map<String, Object>> crearCategoria(@RequestBody Map<String, String> payload) {
        try {
            String nombre = payload.get("nombreDepartamento");

            // Crear y guardar la nueva categoría
            CategoryEntity nuevaCategoria = new CategoryEntity(nombre);
            CategoryEntity categoriaGuardada = categoryRepository.save(nuevaCategoria);

            Map<String, Object> response = new HashMap<>();
            response.put("id", categoriaGuardada.getCategoryId());
            response.put("nombreDepartamento", categoriaGuardada.getCategoryName());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint para actualizar categoría
    @PutMapping("/categorias/{id}")
    @PermitAll
    public ResponseEntity<Map<String, Object>> actualizarCategoria(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            String nombre = payload.get("nombreDepartamento");

            // Buscar la categoría existente
            Optional<CategoryEntity> categoriaExistente = categoryRepository.findById(id);
            if (!categoriaExistente.isPresent()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            // Actualizar y guardar
            CategoryEntity categoria = categoriaExistente.get();
            categoria.setCategoryName(nombre);
            CategoryEntity categoriaActualizada = categoryRepository.save(categoria);

            Map<String, Object> response = new HashMap<>();
            response.put("id", categoriaActualizada.getCategoryId());
            response.put("nombreDepartamento", categoriaActualizada.getCategoryName());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/categorias/{id}")
    @PermitAll
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Categoría eliminada correctamente");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (ExceptionCategoryBadRequest e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        } catch (ExceptionCategoryNotFound e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Ocurrió un error interno del servidor al intentar eliminar la categoría.");
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Generar username automáticamente
    private String generateUsername(String fullName) {
        String[] parts = fullName.split(" ");
        String firstName = parts[0].toLowerCase();
        String lastName = parts.length > 1 ? parts[parts.length - 1].toLowerCase() : "";
        return firstName + "." + lastName;
    }
}