package H2C_Group.H2C_API.Repositories;


import H2C_Group.H2C_API.Entities.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
