package H2C_Group.H2C_API.Services;


import H2C_Group.H2C_API.Entities.ResponseTemplateEntity;
import H2C_Group.H2C_API.Enums.Category;
import H2C_Group.H2C_API.Exceptions.ExceptionResponseTemplateNotFound;
import H2C_Group.H2C_API.Models.DTO.CategoryDTO;
import H2C_Group.H2C_API.Models.DTO.ResponseTemplateDTO;
import H2C_Group.H2C_API.Repositories.ResponseTemplateRepository;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResponseTemplateService {
    @Autowired
    private ResponseTemplateRepository responseTemplateRepository;

    public Page<ResponseTemplateDTO> findAllResponseTemplates(Pageable pageable) {
        Page<ResponseTemplateEntity> responseTemplates = responseTemplateRepository.findAll(pageable);
        return responseTemplates.map(this::convertToResponseTemplateDTO);
    }

    public ResponseTemplateDTO createResponseTemplate(ResponseTemplateDTO responseTemplateDTO) {
        //Valiaciones
        if (responseTemplateDTO.getTitle() == null || responseTemplateDTO.getTitle().trim().isBlank()) {
            throw new IllegalArgumentException("El título de la plantilla no puede ser nulo o vacío.");
        }
        if (responseTemplateDTO.getTitle().trim().length() > 100) {
            throw new IllegalArgumentException("El título de la plantilla excede la longitud máxima permitida (100 caracteres).");
        }
        if (responseTemplateDTO.getTemplateContent() == null || responseTemplateDTO.getTemplateContent().trim().isBlank()) {
            throw new IllegalArgumentException("El contenido de la plantilla no puede ser nulo o vacío.");
        }
        if (responseTemplateDTO.getTemplateContent().trim().length() > 600) {
            throw new IllegalArgumentException("El contenido de la plantilla excede la longitud máxima permitida (600 caracteres).");
        }

        ResponseTemplateEntity responseTemplateEntity = new ResponseTemplateEntity();

        responseTemplateEntity.setTitle(responseTemplateDTO.getTitle());
        responseTemplateEntity.setTemplateContent(responseTemplateDTO.getTemplateContent());
        responseTemplateEntity.setTemplateId(responseTemplateDTO.getTemplateId());

        Category category = Category.fromId(responseTemplateDTO.getCategory().getId()).orElseThrow(() -> new IllegalArgumentException("La categoria de id " + responseTemplateDTO.getCategory().getId() + "  no existe."));
        responseTemplateEntity.setCategoryId(responseTemplateDTO.getCategory().getId());

        responseTemplateRepository.save(responseTemplateEntity);
        return convertToResponseTemplateDTO(responseTemplateEntity);

    }

    public ResponseTemplateDTO updateResponseTemplate(Long id, ResponseTemplateDTO responseTemplateDTO) {

        ResponseTemplateEntity existingTemplate = responseTemplateRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("El id de plantilla " + id + " no existe."));



        if (responseTemplateDTO.getCategory() != null) {
            CategoryDTO categoryFromDTO = responseTemplateDTO.getCategory();
            if (categoryFromDTO.getId() == null) { // Validación de ID obligatoria
                throw new IllegalArgumentException("El ID de categoría es obligatorio.");
            }
            Category categoryEnum = Category.fromIdOptional(categoryFromDTO.getId())
                    .orElseThrow(() -> new IllegalArgumentException("El ID de categoría proporcionado no existe en la enumeración de Categoría: " + categoryFromDTO.getId()));

            if (!categoryEnum.getDisplayName().equalsIgnoreCase(categoryFromDTO.getDisplayName())) {
                throw new IllegalArgumentException("El 'displayName' de la categoría ('" + categoryFromDTO.getDisplayName() + "') no coincide con el 'id' proporcionado (" + categoryFromDTO.getId() + "). Se esperaba: '" + categoryEnum.getDisplayName() + "'");
            }

            existingTemplate.setCategoryId(categoryEnum.getId());
        }

        if (responseTemplateDTO.getTitle() == null || responseTemplateDTO.getTitle().trim().isBlank()) {
            existingTemplate.setTitle(existingTemplate.getTitle());
        }

        if (responseTemplateDTO.getTemplateContent() == null || responseTemplateDTO.getTemplateContent().trim().isBlank()) {
            existingTemplate.setTemplateContent(existingTemplate.getTemplateContent());
        }

        if (responseTemplateDTO.getKeyWords() == null || responseTemplateDTO.getKeyWords().trim().isBlank()) {
            existingTemplate.setKeywords(existingTemplate.getKeywords());
        }

        ResponseTemplateEntity savedTemplate = responseTemplateRepository.save(existingTemplate);
        return convertToResponseTemplateDTO(savedTemplate);
    }

    public void  deleteResponseTemplate(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID de la solucion no puede ser nulo o no válido");
        }

        boolean exists = responseTemplateRepository.existsById(id);

        if (!exists) {
            throw new ExceptionResponseTemplateNotFound("Solucion con ID " + id + " no encontrado.");
        }

        responseTemplateRepository.deleteById(id);
    }


    private ResponseTemplateDTO convertToResponseTemplateDTO(ResponseTemplateEntity responseTemplateEntity) {
        ResponseTemplateDTO responseTemplateDTO = new ResponseTemplateDTO();

        responseTemplateDTO.setTemplateId(responseTemplateEntity.getTemplateId());

        Category categoryEnum = Category.fromIdOptional(responseTemplateEntity.getCategoryId()).orElseThrow(() -> new IllegalArgumentException("La categoria de id " + responseTemplateEntity.getCategoryId() + " no existe."));
        responseTemplateDTO.setCategory(new CategoryDTO(categoryEnum.getId(), categoryEnum.getDisplayName()));

        responseTemplateDTO.setTitle(responseTemplateEntity.getTitle());

        responseTemplateDTO.setTemplateContent(responseTemplateEntity.getTemplateContent());

        responseTemplateDTO.setKeyWords(responseTemplateEntity.getKeywords());

        return responseTemplateDTO;

    }

}
