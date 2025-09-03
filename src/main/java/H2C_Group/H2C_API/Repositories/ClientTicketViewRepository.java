package H2C_Group.H2C_API.Repositories;

import H2C_Group.H2C_API.Entities.Views.ClientTicketView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

// El repositorio ahora extiende JpaSpecificationExecutor
public interface ClientTicketViewRepository extends JpaRepository<ClientTicketView, Long>, JpaSpecificationExecutor<ClientTicketView> {
}