package H2C_Group.H2C_API.Services;


import H2C_Group.H2C_API.Entities.CategoryEntity;
import H2C_Group.H2C_API.Entities.SolutionEntity;
import H2C_Group.H2C_API.Entities.UserEntity;
import H2C_Group.H2C_API.Enums.Category;
import H2C_Group.H2C_API.Exceptions.ExceptionSolutionBadRequest;
import H2C_Group.H2C_API.Exceptions.ExceptionSolutionNotFound;
import H2C_Group.H2C_API.Models.DTO.CategoryDTO;
import H2C_Group.H2C_API.Models.DTO.SolutionDTO;
import H2C_Group.H2C_API.Repositories.CategoryRepository;
import H2C_Group.H2C_API.Repositories.SolutionRepository;
import H2C_Group.H2C_API.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class SolutionService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SolutionRepository solutionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;


    public Page<SolutionDTO> getAllSolutions(Pageable pageable) {
        Page<SolutionEntity> solutions = solutionRepository.findAll(pageable);
        return solutions.map(this::convertToSolutionDTO);
    }

    public SolutionDTO createSolution(SolutionDTO solutionDTO) {
        //Validaciones
        UserEntity existingUser = userRepository.findById(solutionDTO.getUserId()).orElseThrow(() -> new IllegalArgumentException("El usuario con id" + solutionDTO.getUserId() + " no existe"));

        SolutionEntity solutionEntity = new SolutionEntity();


        solutionEntity.setUser(existingUser);

        // Obtiene la entidad de categoría completa del repositorio
        CategoryEntity categoryEntity = categoryRepository.findById(solutionDTO.getCategory().getId())
                .orElseThrow(() -> new IllegalArgumentException("La categoria con id " + solutionDTO.getCategory().getId() + " no existe."));

// Asigna el objeto de categoría completo a la entidad de solución
        solutionEntity.setCategory(categoryEntity);

        // CORRECCIÓN CRÍTICA: Se elimina esta línea. El ID se autogenera.
        // solutionEntity.setSolutionId(solutionDTO.getSolutionId());

        solutionEntity.setSolutionTitle(solutionDTO.getSolutionTitle());
        solutionEntity.setDescriptionS(solutionDTO.getDescriptionS());
        solutionEntity.setSolutionSteps(solutionDTO.getSolutionSteps());
        solutionEntity.setKeyWords(solutionDTO.getKeyWords());

        SolutionEntity savedSolutionEntity = solutionRepository.save(solutionEntity);

        //Notificación para el técnico
        String notificationMessage = "Tu solución '" + savedSolutionEntity.getSolutionTitle() + "' ha sido agregada a la Base de Conocimientos exitosamente.";
        String userId = String.valueOf(existingUser.getUserId());

        // El mensaje se envía solo al técnico que realizó la acción
        messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", notificationMessage);

        return convertToSolutionDTO(savedSolutionEntity);
    }

    public SolutionDTO updateSolution(Long id, SolutionDTO solutionDTO) {
        //Validaciones
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID de la solucion a actualizar no puede ser nulo o no válido.");
        }

        SolutionEntity existingSolution = solutionRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("La solución con ID " + id + " no existe."));

        UserEntity existingUser = userRepository.findById(solutionDTO.getUserId()).orElseThrow(() -> new IllegalArgumentException("El usuario con id" + solutionDTO.getUserId() + " no existe"));
        existingSolution.setUser(existingUser); // Asignar el usuario al que se le hace el update.

        if (solutionDTO.getCategory() != null) {
            CategoryDTO categoryFromDTO = solutionDTO.getCategory();

            if (categoryFromDTO.getId() == null) {
                // Lanza tu excepción controlada:
                throw new ExceptionSolutionBadRequest("El ID de categoría es obligatorio.");
            }

            // Solo necesitamos el ID para encontrar la entidad de Categoría
            CategoryEntity categoryEntity = categoryRepository.findById(categoryFromDTO.getId())
                    .orElseThrow(() -> new ExceptionSolutionNotFound("El ID de categoría " + categoryFromDTO.getId() + " no existe."));

            // Asigna el objeto de categoría a la entidad de solución.
            existingSolution.setCategory(categoryEntity);
        }

        if (solutionDTO.getSolutionTitle() != null) {
            existingSolution.setSolutionTitle(solutionDTO.getSolutionTitle());
        }

        if (solutionDTO.getSolutionSteps() != null) {
            existingSolution.setSolutionSteps(solutionDTO.getSolutionSteps());
        }

        if (solutionDTO.getKeyWords() != null) {
            existingSolution.setKeyWords(solutionDTO.getKeyWords());
        }

        if (solutionDTO.getDescriptionS() != null) {
            existingSolution.setDescriptionS(solutionDTO.getDescriptionS());
        }

        SolutionEntity savedSolutionEntity = solutionRepository.save(existingSolution);

        //Notificación para el técnico
        String notificationMessage = "La solución '" + savedSolutionEntity.getSolutionTitle() + "' ha sido actualizada exitosamente.";
        String userId = String.valueOf(existingSolution.getUser().getUserId());

        // Notificar al usuario que modificó (que ya está asignado a existingSolution.getUser())
        messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", notificationMessage);


        return convertToSolutionDTO(savedSolutionEntity);

    }

    public void deleteSolution(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID de la solucion no puede ser nulo o no válido");
        }

        boolean exists = solutionRepository.existsById(id);

        if (!exists) {
            throw new ExceptionSolutionNotFound("Solucion con ID " + id + " no encontrado.");
        }

        solutionRepository.deleteById(id);
    }




    private SolutionDTO convertToSolutionDTO(SolutionEntity solutionEntity) {
        SolutionDTO solutionDTO = new SolutionDTO();

        if (solutionEntity.getUser() != null) {
            solutionDTO.setUserId(solutionEntity.getUser().getUserId());
        }

        // Accede al objeto category y luego a su ID.
        Category categoryEnum = Category.fromIdOptional(solutionEntity.getCategory().getCategoryId()).orElseThrow(() -> new IllegalArgumentException("La categoria de id " + solutionEntity.getCategory().getCategoryId() + " no existe."));

        // Mapeo del objeto CategoryDTO
        solutionDTO.setCategory(new CategoryDTO(categoryEnum.getId(), categoryEnum.getDisplayName()));

        solutionDTO.setSolutionId(solutionEntity.getSolutionId());
        solutionDTO.setSolutionTitle(solutionEntity.getSolutionTitle());
        solutionDTO.setDescriptionS(solutionEntity.getDescriptionS());
        solutionDTO.setSolutionSteps(solutionEntity.getSolutionSteps());
        solutionDTO.setKeyWords(solutionEntity.getKeyWords());

        // CORRECCIÓN CRÍTICA: Mapear la fecha de actualización
        solutionDTO.setUpdateDate(solutionEntity.getUpdateDate());

        return solutionDTO;

    }

    public Page<SolutionDTO> findByTitle (String value, Pageable pageable) {
        Page<SolutionEntity> entities = solutionRepository.searchBySolutionTitleOrKeyWords(value, pageable);
        return entities.map(this::convertToSolutionDTO);
    }

    public Page<SolutionDTO> getSolutionsByCategory(Long categoryId, Pageable pageable) {
        // Valida que la categoría exista antes de consultar
        if (!categoryRepository.existsById(categoryId)) {
            throw new ExceptionSolutionNotFound("La categoría con ID " + categoryId + " no existe.");
        }

        // Llama a la nueva consulta del repositorio
        Page<SolutionEntity> solutions = solutionRepository.findByCategory_CategoryId(categoryId, pageable);

        // Mapea y retorna los DTOs
        return solutions.map(this::convertToSolutionDTO);
    }
}