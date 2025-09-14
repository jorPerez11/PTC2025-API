package H2C_Group.H2C_API.Repositories;

import H2C_Group.H2C_API.Entities.SolutionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SolutionRepository extends JpaRepository<SolutionEntity,Long> {
    // **NUEVA QUERY PARA CARGAR EL USUARIO**
    @Query("SELECT s FROM SolutionEntity s JOIN FETCH s.user")
    Page<SolutionEntity> findAll(Pageable pageable);

    // *** NUEVA QUERY PARA FILTRAR POR CATEGORÍA ***
    @Query("SELECT s FROM SolutionEntity s JOIN FETCH s.user u WHERE s.category.categoryId = :categoryId")
    Page<SolutionEntity> findByCategory_CategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT s FROM SolutionEntity s " +
            "WHERE LOWER(s.solutionTitle) LIKE LOWER(CONCAT('%', :value, '%')) " +
            "   OR LOWER(s.keyWords) LIKE LOWER(CONCAT('%', :value, '%'))")
    Page<SolutionEntity> searchBySolutionTitleOrKeyWords(@Param("value") String value, Pageable pageable);

    boolean existsByCategory_CategoryId(Long categoryId);
}
