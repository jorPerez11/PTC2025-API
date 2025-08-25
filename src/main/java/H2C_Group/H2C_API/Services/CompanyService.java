package H2C_Group.H2C_API.Services;


import H2C_Group.H2C_API.Entities.CompanyEntity;
import H2C_Group.H2C_API.Exceptions.ExceptionCompanyNotFound;
import H2C_Group.H2C_API.Models.DTO.CompanyDTO;
import H2C_Group.H2C_API.Repositories.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CompanyService {
    @Autowired
    private CompanyRepository repo;
    @Autowired
    private CompanyRepository companyRepository;


    public List<CompanyDTO> findAll() {
        List<CompanyEntity> companies = repo.findAll();
        return companies.stream().map(this::convertToCompanyDTO).collect(Collectors.toList());
    }

    public CompanyDTO updateCompany(Long id, Map<String, String> updates) throws ExceptionCompanyNotFound {
        // 1. Encontrar la compañía por su ID
        CompanyEntity company = companyRepository.findById(id)
                .orElseThrow(() -> new ExceptionCompanyNotFound("Compañía no encontrada con ID: " + id));

        // 2. Iterar sobre el Map de actualizaciones y aplicar los cambios
        updates.forEach((key, value) -> {
            switch (key) {
                case "companyName":
                    company.setCompanyName(value);
                    break;
                case "emailCompany":
                    // Lógica de validación para el email único
                    if (updates.containsKey("emailCompany")) {
                        String newEmail = updates.get("emailCompany");
                        Optional<CompanyEntity> companyWithSameEmail = companyRepository.findByEmailCompany(newEmail);

                        if (companyWithSameEmail.isPresent() && !companyWithSameEmail.get().getCompanyId().equals(id)) {
                            throw new DataIntegrityViolationException("Ya existe una compañía con este correo electrónico.");
                        }
                        company.setEmailCompany(newEmail);
                    }
                    break;
                case "contactPhone":
                    company.setContactPhone(value);
                    break;
                case "websiteUrl":
                    company.setWebsiteUrl(value);
                    break;
                // Asegúrate de agregar cualquier otro campo que se pueda actualizar
            }
        });

        // 3. Guardar la compañía actualizada en la base de datos
        CompanyEntity updatedCompany = companyRepository.save(company);

        // 4. Convertir la entidad a DTO y devolverla
        return convertToDTO(updatedCompany);
    }

    // Asegúrate de tener un metodo convertToDTO que mapee la entidad a un DTO
    private CompanyDTO convertToDTO(CompanyEntity company) {
        CompanyDTO dto = new CompanyDTO();
        dto.setId(company.getCompanyId());
        dto.setCompanyName(company.getCompanyName());
        dto.setEmailCompany(company.getEmailCompany());
        dto.setContactPhone(company.getContactPhone());
        dto.setWebsiteUrl(company.getWebsiteUrl());
        return dto;
    }


    public CompanyDTO registerNewCompany(CompanyDTO dto) {

        //Validaciones
        if (dto.getEmailCompany() == null || dto.getEmailCompany().isEmpty()) {
            throw new IllegalArgumentException("El email de la compañía no puede ser nulo o vacío.");
        }

        if (dto.getCompanyName().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la compañía no puede ser nulo");
        }

        //Conversion DTO -> Entity

        CompanyEntity companyEntity = new CompanyEntity();
        companyEntity.setCompanyName(dto.getCompanyName());
        companyEntity.setEmailCompany(dto.getEmailCompany());
        companyEntity.setContactPhone(dto.getContactPhone());
        companyEntity.setWebsiteUrl(dto.getWebsiteUrl());

        CompanyEntity savedCompany = companyRepository.save(companyEntity);

        return convertToCompanyDTO(savedCompany);

    }



    //Conversion CompanyEntity a CompanyDTO
    private CompanyDTO convertToCompanyDTO(CompanyEntity company) {
        CompanyDTO dto = new CompanyDTO();
        dto.setId(company.getCompanyId());
        dto.setCompanyName(company.getCompanyName());
        dto.setEmailCompany(company.getEmailCompany());
        dto.setContactPhone(company.getContactPhone());
        dto.setWebsiteUrl(company.getWebsiteUrl());
        return dto;
    }

    public List<CompanyDTO> getAllCompanies() {
        return findAll();
    }
}
