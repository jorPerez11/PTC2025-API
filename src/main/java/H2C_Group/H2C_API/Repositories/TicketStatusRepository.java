package H2C_Group.H2C_API.Repositories;

import H2C_Group.H2C_API.Entities.TicketStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketStatusRepository extends JpaRepository<TicketStatusEntity, Integer> {
    // Método para encontrar un estado de ticket por su nombre
    Optional<TicketStatusEntity> findByStatus(String status);
}
