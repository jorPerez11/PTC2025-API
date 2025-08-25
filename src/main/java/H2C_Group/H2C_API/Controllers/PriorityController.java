package H2C_Group.H2C_API.Controllers;

import H2C_Group.H2C_API.Models.DTO.TicketPriorityDTO;
import H2C_Group.H2C_API.Services.PriorityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PriorityController {

    @Autowired
    PriorityService priorityService;

    @GetMapping("/priority")
    public ResponseEntity<List<TicketPriorityDTO>> getPririties(){
        return ResponseEntity.ok(priorityService.getAllPriorities());
    }

}
