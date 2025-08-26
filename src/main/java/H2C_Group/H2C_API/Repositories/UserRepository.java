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
    Optional<UserEntity> findByEmailIgnoreCase(String email);
    Optional<UserEntity> findByPhone(String phone);

    // MÉTODO ÚNICO: findByRolId
    List<UserEntity> findByRolId(Long rolId);

    /**
     * Verifica si existe al menos un usuario asociado a un ID de categoría específico.
     * @param categoryId El ID de la categoría a verificar.
     * @return true si existe al menos un usuario, false en caso contrario.
     */
    boolean existsByCategory_CategoryId(Long categoryId);

    Optional<UserEntity> findByUsername(String username);

    List<UserEntity> findByRolIdInAndCategory_CategoryId(List<Long> roleIds, Long categoryId);


}