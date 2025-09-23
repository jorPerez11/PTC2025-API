package H2C_Group.H2C_API.Controllers;

import H2C_Group.H2C_API.Exceptions.ExceptionAuditTrailBadRequest;
import H2C_Group.H2C_API.Exceptions.ExceptionAuditTrailNotFound;
import H2C_Group.H2C_API.Exceptions.ExceptionCommentBadRequest;
import H2C_Group.H2C_API.Exceptions.ExceptionCommentNotFound;
import H2C_Group.H2C_API.Models.DTO.ActivityDTO;
import H2C_Group.H2C_API.Models.DTO.AuditTrailDTO;
import H2C_Group.H2C_API.Models.DTO.CommentDTO;
import H2C_Group.H2C_API.Repositories.ActivityRepository;
import H2C_Group.H2C_API.Services.ActivityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ActivityController {
    @Autowired
    ActivityRepository activityRepository;

    @Autowired
    ActivityService acceso;

    @GetMapping("/GetActivities")
    public ResponseEntity<Page<ActivityDTO>> getActivities(
            @PageableDefault(page = 0, size = 10)
            Pageable pageable) {
        Page<ActivityDTO> auditTrail = acceso.getAllActivities(pageable);
        return new ResponseEntity<>(auditTrail,  HttpStatus.OK);
    }

    @PostMapping("/PostActivity")
    public ResponseEntity<?> postActivity(@RequestBody @Valid ActivityDTO dto) {
        try{
            ActivityDTO newActivity =  acceso.createActivity(dto);
            return new ResponseEntity<>(newActivity, HttpStatus.CREATED);
        }catch(ExceptionAuditTrailBadRequest e){
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST); // Código 400
        }catch(ExceptionAuditTrailNotFound e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND);
        }catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            e.printStackTrace();
            errors.put("error", "Ocurrió un error interno del servidor al crear la actividad.");
            return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR); // Código 500
        }
    }

    @PatchMapping("/UpdateActivity/{id}")
    public ResponseEntity<?> updateActivity(@PathVariable Long id, @Valid @RequestBody ActivityDTO dto) {
        try{
            ActivityDTO updatedComment = acceso.updateActivity(id, dto);
            return  new ResponseEntity<>(updatedComment, HttpStatus.OK);
        }catch (ExceptionCommentBadRequest e){
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }catch (ExceptionCommentNotFound e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ocurrió un error interno del servidor al actualizar la actividad.");
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/DeleteActivity/{id}")
    public ResponseEntity<?> deleteActivity(@PathVariable Long id) {
        try{
            acceso.deleteActivity(id);
            return  new ResponseEntity<>(HttpStatus.OK);
        }catch (ExceptionCommentBadRequest e){
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }catch(ExceptionCommentNotFound e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ocurrió un error interno del servidor al eliminar la actividad.");
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
