package H2C_Group.H2C_API.Controllers;

import H2C_Group.H2C_API.Entities.Views.ClientTicketView;
import H2C_Group.H2C_API.Services.ClientViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clients")
public class ClientViewController {

    @Autowired
    private ClientViewService clientViewService;

    @GetMapping("/getAllClients")
    public Page<ClientTicketView> getAllClients(
            Pageable pageable,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String search) {

        return clientViewService.getAllClients(pageable, status, period, search);
    }
}