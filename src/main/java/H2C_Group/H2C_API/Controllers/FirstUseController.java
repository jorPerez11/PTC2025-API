package H2C_Group.H2C_API.Controllers;

import H2C_Group.H2C_API.Entities.CategoryEntity;
import H2C_Group.H2C_API.Exceptions.ExceptionCategoryBadRequest;
import H2C_Group.H2C_API.Exceptions.ExceptionCategoryNotFound;
import H2C_Group.H2C_API.Exceptions.ExceptionUserBadRequest;
import H2C_Group.H2C_API.Exceptions.ExceptionUserNotFound;
import H2C_Group.H2C_API.Models.DTO.CategoryDTO;
import H2C_Group.H2C_API.Models.DTO.RolDTO;
import H2C_Group.H2C_API.Models.DTO.UserDTO;
import H2C_Group.H2C_API.Repositories.CategoryRepository;
import H2C_Group.H2C_API.Services.CategoryService;
import H2C_Group.H2C_API.Services.UserService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
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

    // Endpoint para obtener técnicos
    @GetMapping("/tecnicoData")
    public ResponseEntity<?> getTecnicos() {
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
            System.out.println("Error al obtener técnicos: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener técnicos");
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/tecnicos-pendientes")
    @PermitAll
    public ResponseEntity<?> createTechnicianPending(@RequestBody Map<String, Object> payload) {
        try {
            UserDTO userDTO = new UserDTO();
            userDTO.setName((String) payload.get("Nombre"));
            userDTO.setEmail((String) payload.get("Correo Electrónico"));
            userDTO.setPhone((String) payload.get("Número de tel."));
            userDTO.setProfilePictureUrl((String) payload.get("Foto"));
            userDTO.setUsername(generateUsername(userDTO.getName()));

            // --- Validar si el companyId existe antes de usarlo ---
            Long companyId = null;
            Object companyIdObj = payload.get("companyId");
            if (companyIdObj instanceof Number) {
                companyId = ((Number) companyIdObj).longValue();
            } else {
                // Manejar el caso donde companyId es null o no es un número.
                // Esto evita el NullPointerException.
                throw new IllegalArgumentException("companyId no es válido o está ausente.");
            }

            // El servicio se encargará de buscar la compañía y asignar el rol de técnico
            UserDTO savedUser = userService.registerTechnicianPending(userDTO, companyId); // Pasamos companyId al servicio

            Map<String, Object> response = new HashMap<>();
            response.put("id", savedUser.getId());
            response.put("Nombre", savedUser.getName());
            response.put("Correo Electrónico", savedUser.getEmail());
            response.put("Mensaje", "Técnico creado en estado pendiente.");

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al crear técnico pendiente: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint para actualizar técnico
    @PutMapping("/tecnicoData/{id}")
    public ResponseEntity<?> updateTecnico(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            UserDTO updatedUser = userService.UpdateUser(id, payload); // Pasa el payload directamente

            Map<String, Object> response = new HashMap<>();
            response.put("id", updatedUser.getId());
            response.put("Nombre", updatedUser.getName());
            response.put("Correo Electrónico", updatedUser.getEmail());
            response.put("Número de tel.", updatedUser.getPhone());
            response.put("Foto", updatedUser.getProfilePictureUrl());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        } catch (ExceptionUserNotFound e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            System.out.println("Error al actualizar técnico: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al actualizar técnico");
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Nuevo Endpoint para asignar categoría y activar a un técnico
    @PatchMapping("/tecnicos/{id}/asignar-categoria")
    @PermitAll
    public ResponseEntity<?> assignCategoryToTechnician(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        try {
            Long categoryId = ((Number) payload.get("categoryId")).longValue();
            UserDTO updatedUser = userService.assignCategoryAndActivateTechnician(id, categoryId);

            Map<String, Object> response = new HashMap<>();
            response.put("id", updatedUser.getId());
            response.put("Nombre", updatedUser.getName());
            response.put("Correo Electrónico", updatedUser.getEmail());
            response.put("Categoría Asignada", updatedUser.getCategory().getDisplayName());
            response.put("Mensaje", "Técnico activado y credenciales enviadas por correo electrónico.");

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        } catch (ExceptionUserNotFound | ExceptionCategoryNotFound e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error interno al asignar categoría y activar técnico");
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint para eliminar técnico
    @DeleteMapping("/tecnicoData/{id}")
    public ResponseEntity<?> deleteTecnico(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        } catch (ExceptionUserNotFound e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            System.out.println("Error al eliminar técnico: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al eliminar técnico");
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
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

    @PostMapping("/finalize-admin/{userId}")
    public ResponseEntity<?> finalizeAdmin(@PathVariable Long userId) {
        try {
            UserDTO finalUser = userService.finalizeAdminSetup(userId);
            return new ResponseEntity<>(finalUser, HttpStatus.OK);
        } catch (ExceptionUserNotFound e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ocurrió un error al finalizar la configuración: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Nuevo Endpoint para el registro inicial del administrador
    @PostMapping("/register-admin")
    @PermitAll
    public ResponseEntity<?> registerInitialAdmin(@RequestBody UserDTO userDTO) {
        try {
            UserDTO newUser = userService.registerInitialAdmin(userDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("id", newUser.getId());
            response.put("Nombre", newUser.getName());
            response.put("Correo Electrónico", newUser.getEmail());
            response.put("Mensaje", "Usuario administrador registrado en estado pendiente.");

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            e.printStackTrace();
            error.put("error", "Ocurrió un error al registrar al administrador inicial: " + e.getMessage());
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