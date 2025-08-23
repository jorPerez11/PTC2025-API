package H2C_Group.H2C_API.Controllers;

import H2C_Group.H2C_API.Exceptions.ExceptionSolutionBadRequest;
import H2C_Group.H2C_API.Exceptions.ExceptionSolutionNotFound;
import H2C_Group.H2C_API.Models.DTO.SolutionDTO;
import H2C_Group.H2C_API.Models.DTO.TicketDTO;
import H2C_Group.H2C_API.Repositories.SolutionRepository;
import H2C_Group.H2C_API.Services.SolutionService;
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
public class SolutionController {
    @Autowired
    SolutionService acceso;

    @GetMapping("/GetSolutions")
    public ResponseEntity<Page<SolutionDTO>> getAllSolutions(
            @PageableDefault(page = 0, size = 10)
            Pageable pageable
    ) {
        Page<SolutionDTO> solutions = acceso.getAllSolutions(pageable);
        return new ResponseEntity<>(solutions, HttpStatus.OK);
    }


    @PostMapping("/PostSolution")
    public ResponseEntity<?> createSolution(@RequestBody @Valid SolutionDTO solutionDTO) {
        try {
            SolutionDTO newSolution = acceso.createSolution(solutionDTO);
            return new ResponseEntity<>(newSolution, HttpStatus.CREATED);
        }catch (ExceptionSolutionBadRequest e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST); // Código 400
        }catch (ExceptionSolutionNotFound e) {
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
    public ResponseEntity<?> updateSolution(@PathVariable Long id, @Valid @RequestBody SolutionDTO solutionDTO) {
        try{
            SolutionDTO newSolution = acceso.updateSolution(id, solutionDTO);
            return new ResponseEntity<>(newSolution, HttpStatus.OK);
        } catch (ExceptionSolutionBadRequest e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors.toString(), HttpStatus.BAD_REQUEST);
        }catch (ExceptionSolutionNotFound e){
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors.toString(), HttpStatus.NOT_FOUND);
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
        }catch (ExceptionSolutionBadRequest e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors.toString(), HttpStatus.BAD_REQUEST);
        }catch (ExceptionSolutionNotFound e){
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors.toString(), HttpStatus.NOT_FOUND);
        }catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", "Ocurrió un error interno del servidor al intentar eliminar la solucion.");
            return new ResponseEntity<>(errors.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //Búsqueda
    @GetMapping("/searchSolution")
    public ResponseEntity<?> findSolutions(@RequestParam String title) {
        List<SolutionDTO> results = acceso.findByTitle(title);
        if (results.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ninguna solución coincide con la búsqueda");
        }
        return ResponseEntity.ok(results);
    }
}
