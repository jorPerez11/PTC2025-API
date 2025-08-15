package H2C_Group.H2C_API.Controllers;

import H2C_Group.H2C_API.Exceptions.*;
import H2C_Group.H2C_API.Models.DTO.SurveyDTO;
import H2C_Group.H2C_API.Services.SurveyService;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
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
public class SurveyController {
    @Autowired
    private SurveyService acceso;


    @GetMapping("/GetSurveys")
    public ResponseEntity<Page<SurveyDTO>> getSurvey(
            @PageableDefault(page = 0, size = 10)
            Pageable pageable){
        Page<SurveyDTO> survey =  acceso.getAllSurveys(pageable);
        return new ResponseEntity<>(survey, HttpStatus.OK);
    }

    @PostMapping("/PostSurvey")
    public ResponseEntity<?> postSurvey(@Valid @RequestBody SurveyDTO surveyDTO) {
        try{
            SurveyDTO survey = acceso.createSurvey(surveyDTO);
            return new ResponseEntity<>(survey, HttpStatus.CREATED);
        }catch (ExceptionSurveyBadRequest e) {
            //Validación de argumentos invalidos
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST); // Código 400
        }catch (ExceptionSurveyNotFound e) {
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


}
