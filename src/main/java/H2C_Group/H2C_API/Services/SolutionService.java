package H2C_Group.H2C_API.Services;


import H2C_Group.H2C_API.Entities.CategoryEntity;
import H2C_Group.H2C_API.Entities.SolutionEntity;
import H2C_Group.H2C_API.Entities.TicketEntity;
import H2C_Group.H2C_API.Entities.UserEntity;
import H2C_Group.H2C_API.Enums.Category;
import H2C_Group.H2C_API.Exceptions.ExceptionSolutionNotFound;
import H2C_Group.H2C_API.Models.DTO.CategoryDTO;
import H2C_Group.H2C_API.Models.DTO.SolutionDTO;
import H2C_Group.H2C_API.Models.DTO.TicketDTO;
import H2C_Group.H2C_API.Repositories.CategoryRepository;
import H2C_Group.H2C_API.Repositories.SolutionRepository;
import H2C_Group.H2C_API.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SolutionService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SolutionRepository solutionRepository;

    @Autowired
    private CategoryRepository categoryRepository;


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

        solutionEntity.setSolutionId(solutionDTO.getSolutionId());
        solutionEntity.setSolutionTitle(solutionDTO.getSolutionTitle());
        solutionEntity.setDescriptionS(solutionDTO.getDescriptionS());
        solutionEntity.setSolutionSteps(solutionDTO.getSolutionSteps());
        solutionEntity.setKeyWords(solutionDTO.getKeyWords());

        SolutionEntity savedSolutionEntity = solutionRepository.save(solutionEntity);
        return convertToSolutionDTO(savedSolutionEntity);


    }

    public SolutionDTO updateSolution(Long id, SolutionDTO solutionDTO) {
        //Validaciones
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID de la solucion a actualizar no puede ser nulo o no válido.");
        }

        SolutionEntity existingSolution = solutionRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("La solución con ID " + id + " no existe."));

        UserEntity existingUser = userRepository.findById(solutionDTO.getUserId()).orElseThrow(() -> new IllegalArgumentException("El usuario con id" + solutionDTO.getUserId() + " no existe"));
        existingUser.setUserId(existingUser.getUserId());

        if (solutionDTO.getCategory() != null) {
            CategoryDTO categoryFromDTO = solutionDTO.getCategory();
            if (categoryFromDTO.getId() == null) { // Validación de ID obligatoria
                throw new IllegalArgumentException("El ID de categoría es obligatorio.");
            }
            Category categoryEnum = Category.fromIdOptional(categoryFromDTO.getId())
                    .orElseThrow(() -> new IllegalArgumentException("El ID de categoría proporcionado no existe en la enumeración de Categoría: " + categoryFromDTO.getId()));

            if (!categoryEnum.getDisplayName().equalsIgnoreCase(categoryFromDTO.getDisplayName())) {
                throw new IllegalArgumentException("El 'displayName' de la categoría ('" + categoryFromDTO.getDisplayName() + "') no coincide con el 'id' proporcionado (" + categoryFromDTO.getId() + "). Se esperaba: '" + categoryEnum.getDisplayName() + "'");
            }

            // Obtiene el objeto CategoryEntity desde el repositorio.
            CategoryEntity categoryEntity = categoryRepository.findById(categoryFromDTO.getId())
                    .orElseThrow(() -> new IllegalArgumentException("El ID de categoría proporcionado no existe en la BD: " + categoryFromDTO.getId()));

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

        solutionDTO.setSolutionId(solutionEntity.getSolutionId());
        solutionDTO.setSolutionTitle(solutionEntity.getSolutionTitle());
        solutionDTO.setDescriptionS(solutionEntity.getDescriptionS());
        solutionDTO.setSolutionSteps(solutionEntity.getSolutionSteps());
        solutionDTO.setKeyWords(solutionEntity.getKeyWords());
        return solutionDTO;

    }

    public List<SolutionDTO> findByTitle (String value) {
        List<SolutionEntity> entities = solutionRepository.searchBySolutionTitleOrKeyWords(value);
        return entities.stream()
                .map(this::convertToSolutionDTO)
                .collect(Collectors.toList());
    }
}
