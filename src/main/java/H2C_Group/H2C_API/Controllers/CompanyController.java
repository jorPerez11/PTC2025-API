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
        }
    }

    @PostMapping("/PostCompany")
    public ResponseEntity<?> createCompany(@Valid @RequestBody CompanyDTO company) {
        try {
            CompanyDTO newCompany = acceso.registerNewCompany(company);
            return new ResponseEntity<>(newCompany, HttpStatus.CREATED);
        } catch (DataIntegrityViolationException e) {
            // Atrapa la excepción de restricción única
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ya existe una compañía con este correo electrónico o nombre de usuario.");
            e.printStackTrace(); // Para ver el error completo en el log del servidor
            return new ResponseEntity<>(error, HttpStatus.CONFLICT); // Código 409
        } catch (ExceptionCompanyBadRequest e) {
            // Si hay otros errores de validación
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST); // Código 400
        }
    }

}
