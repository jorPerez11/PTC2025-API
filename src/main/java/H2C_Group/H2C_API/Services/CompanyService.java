package H2C_Group.H2C_API.Services;


import H2C_Group.H2C_API.Entities.CompanyEntity;
import H2C_Group.H2C_API.Models.DTO.CompanyDTO;
import H2C_Group.H2C_API.Repositories.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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
