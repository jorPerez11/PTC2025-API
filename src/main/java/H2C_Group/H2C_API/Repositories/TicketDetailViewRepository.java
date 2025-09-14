package H2C_Group.H2C_API.Repositories;

import H2C_Group.H2C_API.Entities.Views.TicketDetailView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketDetailViewRepository extends JpaRepository<TicketDetailView, Long> {
    Optional<TicketDetailView> findByTicketId(Long ticketId);
}
