package H2C_Group.H2C_API.Repositories;

import H2C_Group.H2C_API.Entities.Views.TicketView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FullTicketDataViewRepository extends JpaRepository<TicketView, Long> {

    // Ahora, Spring Data JPA puede generar la consulta SQL
    // para filtrar correctamente por 'assignedTech'
    List<TicketView> findByAssignedTech(Long assignedTech);
}
