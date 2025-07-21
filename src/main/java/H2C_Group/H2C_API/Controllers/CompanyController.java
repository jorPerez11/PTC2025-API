package H2C_Group.H2C_API.Controllers;


import H2C_Group.H2C_API.Entities.CompanyEntity;
import H2C_Group.H2C_API.Models.DTO.CompanyDTO;
import H2C_Group.H2C_API.Models.DTO.UserDTO;
import H2C_Group.H2C_API.Services.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CompanyController {

    @Autowired
    private CompanyService acceso;

    @GetMapping("/GetCompanies")
    public List<CompanyDTO> datosCompania() {
        return acceso.getAllCompanies();
    }

    @PostMapping("/PostCompany")
    public ResponseEntity<CompanyDTO> createCompany(@RequestBody CompanyDTO company) {
        CompanyDTO newCompany = acceso.registerNewCompany(company);
        return new ResponseEntity<>(newCompany, HttpStatus.CREATED);
    }

}
