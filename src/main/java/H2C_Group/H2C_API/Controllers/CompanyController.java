package H2C_Group.H2C_API.Controllers;


import H2C_Group.H2C_API.Exceptions.ExceptionCompanyBadRequest;
import H2C_Group.H2C_API.Exceptions.ExceptionCompanyNotFound;
import H2C_Group.H2C_API.Models.DTO.CompanyDTO;
import H2C_Group.H2C_API.Services.CompanyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class CompanyController {

    @Autowired
    private CompanyService acceso;

    @GetMapping("/GetCompany")
    public List<CompanyDTO> companyData(    ) {
        return acceso.getAllCompanies();
    }

    @GetMapping("/check-company-existence")
    public ResponseEntity<Boolean> checkCompanyExistence() {
        boolean hasCompanies = acceso.getAllCompanies().size() > 0;
        return ResponseEntity.ok(hasCompanies);
    }

    @PatchMapping("/companies/{id}")
    public ResponseEntity<?> updateCompany(@PathVariable Long id, @RequestBody Map<String, String> updates) {
        try {
            CompanyDTO updatedCompany = acceso.updateCompany(id, updates);
            return new ResponseEntity<>(updatedCompany, HttpStatus.OK);
        } catch (ExceptionCompanyNotFound e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        } catch (DataIntegrityViolationException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ya existe una compañía con este correo electrónico o nombre de usuario.");
            e.printStackTrace();
            return new ResponseEntity<>(error, HttpStatus.CONFLICT); // Código 409
        }
    }

    @PostMapping("/PostCompany")
    public ResponseEntity<?> createCompany(@Valid @RequestBody CompanyDTO company) {
        try {
            CompanyDTO newCompany = acceso.registerNewCompany(company);
            // Devuelve el objeto de la compañía creada
            return new ResponseEntity<>(newCompany, HttpStatus.CREATED);
        } catch (DataIntegrityViolationException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ya existe una compañía con este correo electrónico o nombre de usuario.");
            return new ResponseEntity<>(error, HttpStatus.CONFLICT);
        } catch (ExceptionCompanyBadRequest e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/companies/{id}")
    public ResponseEntity<?> deleteCompany(@PathVariable Long id) {
        try {
            acceso.deleteCompany(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ExceptionCompanyNotFound e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ocurrió un error interno del servidor al eliminar la compañía.");
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
