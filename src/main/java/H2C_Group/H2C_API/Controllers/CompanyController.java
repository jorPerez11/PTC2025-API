package H2C_Group.H2C_API.Controllers;


import H2C_Group.H2C_API.Exceptions.ExceptionCompanyBadRequest;
import H2C_Group.H2C_API.Models.DTO.CompanyDTO;
import H2C_Group.H2C_API.Services.CompanyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CompanyController {

    @Autowired
    private CompanyService acceso;

    @GetMapping("/GetCompany")
    public List<CompanyDTO> companyData(    ) {
        return acceso.getAllCompanies();
    }

    @PostMapping("/PostCompany")
    public ResponseEntity<?> createCompany(@Valid @RequestBody CompanyDTO company) {
        try{
            CompanyDTO newCompany = acceso.registerNewCompany(company);
            return new ResponseEntity<>(newCompany, HttpStatus.CREATED);
        }catch(ExceptionCompanyBadRequest e){
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST); // Código 400
        }

    }

}
