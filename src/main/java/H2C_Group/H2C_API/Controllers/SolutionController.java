package H2C_Group.H2C_API.Controllers;

import H2C_Group.H2C_API.Exceptions.TicketExceptions;
import H2C_Group.H2C_API.Exceptions.UserExceptions;
import H2C_Group.H2C_API.Models.DTO.SolutionDTO;
import H2C_Group.H2C_API.Models.DTO.TicketDTO;
import H2C_Group.H2C_API.Repositories.SolutionRepository;
import H2C_Group.H2C_API.Services.SolutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SolutionController {
    @Autowired
    SolutionService acceso;

    @GetMapping("/GetSolutions")
    public ResponseEntity<List<SolutionDTO>> getAllSolutions() {
        return new ResponseEntity<>(acceso.getAllSolutions(), HttpStatus.OK);
    }


    @PostMapping("/PostSolution")
    public ResponseEntity<?> createSolution(@RequestBody SolutionDTO solutionDTO) {
        try {
            SolutionDTO newSolution = acceso.createSolution(solutionDTO);
            return new ResponseEntity<>(newSolution, HttpStatus.CREATED);
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
            errors.put("error", "Ocurrió un error interno del servidor al crear la solucion.");
            return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR); // Código 500
        }
    }


    @PatchMapping("/UpdateSolution/{id}")
    public ResponseEntity<?> updateSolution(@PathVariable Long id, @RequestBody SolutionDTO solutionDTO) {
        try{
            SolutionDTO newSolution = acceso.updateSolution(id, solutionDTO);
            return new ResponseEntity<>(newSolution, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors.toString(), HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", "Ocurrió un error interno del servidor al actualizar la solucion.");
            return new ResponseEntity<>(errors.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @DeleteMapping("/DeleteSolution/{id}")
    public ResponseEntity<?> deleteSolution(@PathVariable Long id) {
        try {
            acceso.deleteSolution(id);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (IllegalArgumentException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors.toString(), HttpStatus.BAD_REQUEST);
        }catch (TicketExceptions.TicketNotFoundException e){
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors.toString(), HttpStatus.NOT_FOUND);
        }catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", "Ocurrió un error interno del servidor al intentar eliminar la solucion.");
            return new ResponseEntity<>(errors.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
