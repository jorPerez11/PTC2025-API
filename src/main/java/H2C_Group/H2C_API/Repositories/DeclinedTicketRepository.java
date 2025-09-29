package H2C_Group.H2C_API.Repositories;

import H2C_Group.H2C_API.Entities.DeclinedTicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeclinedTicketRepository extends JpaRepository<DeclinedTicketEntity, DeclinedTicketEntity> {

    //Busca si un tecnico ha rechazado un ticket especifico
    boolean existsByTicketIdAndTechnicianId(Long ticketId, Long technicianId);

    //Busca todos los tickets que un tecnico ha rechazado
    List<DeclinedTicketEntity> findByTechnicianId(Long technicianId);
}
