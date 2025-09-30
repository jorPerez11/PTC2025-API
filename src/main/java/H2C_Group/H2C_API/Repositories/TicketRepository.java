package H2C_Group.H2C_API.Repositories;


import H2C_Group.H2C_API.Entities.TicketEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<TicketEntity,Long> {

    Optional<TicketEntity> findByTitle(String title); //SE USARA EN METODO PARA BUSCAR ARTICULO (BASE DE CONOCIMIENTOS)
    /**
     * Verifica si existe al menos un ticket asociado a un ID de categoría específico.
     * @param categoryId El ID de la categoría a verificar.
     * @return true si existe al menos un ticket, false en caso contrario.
     */
    boolean existsByCategoryId(Long categoryId);
    //Busca tickets por el Id del usuario creador y los ordena por fecha de creacion descendente
    List<TicketEntity> findByUserCreator_UserIdOrderByCreationDate(Long userId);

    List<TicketEntity> findByAssignedTechUser_UserId(Long assignedTechUserId);

    long countByAssignedTechUser_UserIdAndTicketStatusIdIn(Long userId, List<Long> statusIds);

    @Query(value = "SELECT TS.STATUS, COUNT(T.TICKETID) FROM TBTICKETS T JOIN TBTICKETSTATUS TS ON T.TICKETSTATUSID = TS.TICKETSTATUSID GROUP BY TS.STATUS", nativeQuery = true)
    List<Object[]> countTicketsByStatus();

    @Query("SELECT t FROM TicketEntity t WHERE t.id = ?1")
    TicketEntity findTicketById(Integer id);

    List<TicketEntity> findByTicketStatusIdAndAssignedTechUserIsNull(Long enEsperaId);

    //Busca todos los tickets que tienen un id de estado de ticket en espera
    List<TicketEntity> findByTicketStatusId(Long enEsperaId);

    // 🚨 Nueva consulta para evitar el problema N+1 y LazyInitializationException
    @Query(value = "SELECT t FROM TicketEntity t " +
            "JOIN FETCH t.userCreator u " + // Creador del ticket
            "LEFT JOIN FETCH t.assignedTechUser a ", // Técnico asignado
            countQuery = "SELECT count(t) FROM TicketEntity t")
    Page<TicketEntity> findAllWithUsers(Pageable pageable);
}
