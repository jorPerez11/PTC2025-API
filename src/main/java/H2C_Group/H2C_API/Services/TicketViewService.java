package H2C_Group.H2C_API.Services;

import H2C_Group.H2C_API.Entities.Views.TicketDetailView;
import H2C_Group.H2C_API.Repositories.TicketDetailViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TicketViewService {
    @Autowired
    private TicketDetailViewRepository ticketDetailViewRepository;

    public Optional<TicketDetailView> getTicketDetails(Long ticketId) {
        return ticketDetailViewRepository.findByTicketId(ticketId);
    }
}
