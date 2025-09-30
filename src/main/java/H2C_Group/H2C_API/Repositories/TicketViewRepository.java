package H2C_Group.H2C_API.Repositories;

import H2C_Group.H2C_API.Entities.Views.TicketView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketViewRepository extends JpaRepository<TicketView, Long> {
    // Nueva función para encontrar tickets por el ID del técnico asignado
    List<TicketView> findByAssignedTech(Long assignedTech);
}
