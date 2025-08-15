package H2C_Group.H2C_API.Controllers;

import H2C_Group.H2C_API.Exceptions.ExceptionResponseTemplateBadRequest;
import H2C_Group.H2C_API.Exceptions.ExceptionResponseTemplateNotFound;
import H2C_Group.H2C_API.Models.DTO.ResponseTemplateDTO;
import H2C_Group.H2C_API.Models.DTO.SolutionDTO;
import H2C_Group.H2C_API.Services.ResponseTemplateService;
import jakarta.validation.Valid;
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
@RequestMapping("/api")
public class ResponseTemplateController {
    @Autowired
    private ResponseTemplateService acceso;

    @GetMapping("/GetTemplates")
    public ResponseEntity<Page<ResponseTemplateDTO>> getAllResponseTemplates(
            @PageableDefault(page = 0, size = 10)
            Pageable pageable) {
        Page<ResponseTemplateDTO> templates = acceso.findAllResponseTemplates(pageable);
        return new ResponseEntity<>(templates, HttpStatus.OK);
    }

    @PostMapping("/PostTemplate")
    public ResponseEntity<?> createResponseTemplate(@RequestBody @Valid ResponseTemplateDTO responseTemplateDTO) {
        try{
            ResponseTemplateDTO newTemplate = acceso.createResponseTemplate(responseTemplateDTO);
            return new ResponseEntity<>(newTemplate, HttpStatus.CREATED);
        }catch (ExceptionResponseTemplateBadRequest e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST); // Código 400
        }catch (ExceptionResponseTemplateNotFound e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND); // Código 404
        }catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            e.printStackTrace();
            errors.put("error", "Ocurrió un error interno del servidor al crear la plantilla.");
            return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR); // Código 500
        }

    }

    @PatchMapping("/UpdateTemplate/{id}")
    public ResponseEntity<?> updateResponseTemplate(@PathVariable Long id, @Valid @RequestBody ResponseTemplateDTO responseTemplateDTO) {
        try{
            ResponseTemplateDTO newTemplate = acceso.updateResponseTemplate(id, responseTemplateDTO);
            return new ResponseEntity<>(newTemplate, HttpStatus.OK);
        }catch (ExceptionResponseTemplateBadRequest e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors.toString(), HttpStatus.BAD_REQUEST);
        }catch (ExceptionResponseTemplateNotFound e){
                Map<String, String> errors = new HashMap<>();
                errors.put("error", e.getMessage());
                return new ResponseEntity<>(errors.toString(), HttpStatus.NOT_FOUND);
        }catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            e.printStackTrace();
            errors.put("error", "Ocurrió un error interno del servidor al actualizar la plantilla.");
            return new ResponseEntity<>(errors.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/DeleteTemplate/{id}")
    public ResponseEntity<?> deleteResponseTemplate(@PathVariable Long id) {
        try{
            acceso.deleteResponseTemplate(id);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (ExceptionResponseTemplateBadRequest e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors.toString(), HttpStatus.BAD_REQUEST);
        }catch (ExceptionResponseTemplateNotFound e){
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors.toString(), HttpStatus.NOT_FOUND);
        }catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", "Ocurrió un error interno del servidor al intentar eliminar la plantilla.");
            return new ResponseEntity<>(errors.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
