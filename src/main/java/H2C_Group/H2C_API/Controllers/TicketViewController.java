package H2C_Group.H2C_API.Controllers;

import H2C_Group.H2C_API.Entities.Views.TicketDetailView;
import H2C_Group.H2C_API.Services.TicketViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/tickets")
public class TicketViewController {

    @Autowired
    private TicketViewService ticketViewService;

    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketDetailView> getTicketDetails(@PathVariable Long ticketId) {
        Optional<TicketDetailView> ticketDetails = ticketViewService.getTicketDetails(ticketId);
        return ticketDetails.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
