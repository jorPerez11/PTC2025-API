package H2C_Group.H2C_API.Controllers;

import H2C_Group.H2C_API.Exceptions.SurveyExceptions;
import H2C_Group.H2C_API.Exceptions.TicketExceptions;
import H2C_Group.H2C_API.Exceptions.UserExceptions;
import H2C_Group.H2C_API.Models.DTO.SurveyDTO;
import H2C_Group.H2C_API.Services.SurveyService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SurveyController {
    @Autowired
    private SurveyService acceso;


    @GetMapping("/GetSurveys")
    public ResponseEntity<List<SurveyDTO>> getSurvey(){
        return new ResponseEntity<>(acceso.getAllSurveys(), HttpStatus.OK);
    }

    @PostMapping("/PostSurvey")
    public ResponseEntity<?> postSurvey(@RequestBody SurveyDTO surveyDTO) {
        try{
            SurveyDTO survey = acceso.createSurvey(surveyDTO);
            return new ResponseEntity<>(survey, HttpStatus.CREATED);
        }catch (IllegalArgumentException e) {
            //Validación de argumentos invalidos
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST); // Código 400
        }catch (UserExceptions.UserNotFoundException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND); // Código 404
        }catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            e.printStackTrace();
            errors.put("error", "Ocurrió un error interno del servidor al crear la encuesta.");
            return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR); // Código 500
        }
    }

    @PatchMapping("/UpdateSurvey/{id}")
    public ResponseEntity<?> updateSurvey(@RequestBody SurveyDTO surveyDTO, @PathVariable Long id) {
        try{
            SurveyDTO survey = acceso.updateSurvey(id, surveyDTO);
            return new ResponseEntity<>(survey, HttpStatus.OK);
        }catch (IllegalArgumentException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors.toString(), HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            e.printStackTrace();
            errors.put("error", "Ocurrió un error interno del servidor al actualizar la encuesta.");
            return new ResponseEntity<>(errors.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/DeleteSurvey/{id}")
    public ResponseEntity<?> deleteSurvey(@PathVariable Long id) {
        try{
            acceso.deleteSurvey(id);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (IllegalArgumentException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors.toString(), HttpStatus.BAD_REQUEST);
        }catch (SurveyExceptions.SurveyNotFoundException e){
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors.toString(), HttpStatus.NOT_FOUND);
        }catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", "Ocurrió un error interno del servidor al intentar eliminar la encuesta.");
            return new ResponseEntity<>(errors.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
