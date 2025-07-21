package H2C_Group.H2C_API.Repositories;


import H2C_Group.H2C_API.Entities.UserEntity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long>{

    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByPhone(String phone);

    Optional<UserEntity> findByUsername(String username);


}
