package H2C_Group.H2C_API.Controllers;


import H2C_Group.H2C_API.Exceptions.CommentExceptions;
import H2C_Group.H2C_API.Models.DTO.AuditTrailDTO;
import H2C_Group.H2C_API.Repositories.AuditTrailRepository;
import H2C_Group.H2C_API.Services.AuditTrailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuditTrailController {
    @Autowired
    private AuditTrailService acceso;


    @GetMapping("/GetAuditTrail")
    public List<AuditTrailDTO> getAuditTrail() {
        return acceso.getAllAuditTrails();
    }

    @PostMapping("/PostAuditTrail")
    public ResponseEntity<?> postAuditTrail(@RequestBody AuditTrailDTO auditTrailDTO) {
        try{
            AuditTrailDTO newAuditTrail =  acceso.createAuditTrail(auditTrailDTO);
            return new ResponseEntity<>(newAuditTrail, HttpStatus.CREATED);
        }catch(IllegalArgumentException e){
            //Validación de argumentos invalidos
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST); // Código 400
        }catch(CommentExceptions.CommentNotFoundException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND);
        }catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            e.printStackTrace();
            errors.put("error", "Ocurrió un error interno del servidor al crear el registro de auditoria.");
            return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR); // Código 500
        }
    }
}
