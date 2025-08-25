package H2C_Group.H2C_API.Repositories;


import H2C_Group.H2C_API.Entities.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long>{

    Optional<UserEntity> findByEmailIgnoreCase(String email);
    Optional<UserEntity> findByPhone(String phone);

    Optional<UserEntity> findByUsername(String username);

    Page<UserEntity> findAll(Pageable pageable);

    Page<UserEntity> findByRolId(Long rolId, Pageable pageable);

    List<UserEntity> findByRolId(Long rolId);

    @Query("SELECT u FROM UserEntity u " +
            // 🔑 CLAVE: Usar LEFT JOIN FETCH para cargar la categoría junto con el usuario.
            "LEFT JOIN FETCH u.category c " +
            "WHERE u.rolId = :roleId AND " +
            "      (:term IS NULL OR :term = '' OR " +
            "       LOWER(u.fullName) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "       LOWER(u.email) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "       CAST(u.userId AS string) = :term ) AND " +
            "      (:categoryId IS NULL OR " +
            "       u.categoryId = :categoryId) " +
            "ORDER BY u.userId DESC")
    Page<UserEntity> findTechUsersWithFilters(
            Pageable pageable,
            @Param("roleId") Long roleId,
            @Param("term") String term,
            @Param("categoryId") Long categoryId,
            String period
    );

    boolean existsByCategory_CategoryId(Long id);
}
