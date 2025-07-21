package H2C_Group.H2C_API.Repositories;


import H2C_Group.H2C_API.Entities.CompanyEntity;
import H2C_Group.H2C_API.Entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {

    @Query("SELECT MIN(c.companyId) FROM CompanyEntity c")
    Optional<Long> findFirstCompanyId(); //Usamos Optional<Long> para manejar el caso de que no haya compañías

}
