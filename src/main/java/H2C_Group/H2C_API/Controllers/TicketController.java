package H2C_Group.H2C_API.Controllers;

import H2C_Group.H2C_API.Exceptions.ExceptionTicketBadRequest;
import H2C_Group.H2C_API.Exceptions.ExceptionTicketNotFound;
import H2C_Group.H2C_API.Models.DTO.TicketDTO;
import H2C_Group.H2C_API.Services.TicketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class TicketController {
    @Autowired
    private TicketService acceso;

    @GetMapping("/GetTickets")
    public ResponseEntity<Page<TicketDTO>> getAllTickets(
            @PageableDefault(page = 0, size = 10)
            Pageable pageable) {
        Page<TicketDTO> ticket =  acceso.getAllTickets(pageable);
        return new ResponseEntity<>(ticket, HttpStatus.OK);
    }

    @GetMapping("/GetRecentTicketsByUser/{userId}")
    public ResponseEntity<List<TicketDTO>> getTicketsByUserId(@PathVariable Long userId){
        List<TicketDTO> tickets = acceso.geTicketByUserId(userId);
        return new ResponseEntity<>(tickets, HttpStatus.OK);
    }

    @GetMapping("/GetAssignedTicketsByTech/{technicianId}")
    public ResponseEntity<List<TicketDTO>> getAssignedTicketsByTechnicianId(@PathVariable Long technicianId){
        List<TicketDTO> tickets = acceso.getAssignedTicketsByTechnicianId(technicianId);
        return new ResponseEntity<>(tickets, HttpStatus.OK);
    }

    @PostMapping("/PostTicket")
    public ResponseEntity<?> postTicket(@Valid @RequestBody TicketDTO ticketDTO) {
        try {
            TicketDTO newTicket = acceso.createTicket(ticketDTO);
            return new ResponseEntity<>(newTicket, HttpStatus.CREATED);
        }catch (ExceptionTicketBadRequest e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST); // Código 400
        }catch (ExceptionTicketNotFound e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND); // Código 404
        }catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", "Ocurrió un error interno del servidor al crear el ticket.");
            return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR); // Código 500
        }

    }

    @PutMapping("/accept/{ticketId}/{technicianId}")
    public ResponseEntity<TicketDTO> acceptTicket(@PathVariable Long ticketId, @PathVariable Long technicianId) {
        TicketDTO acceptedTicket = acceso.acceptTicket(ticketId, technicianId);
        return new ResponseEntity<>(acceptedTicket, HttpStatus.OK);
    }


    @PatchMapping("/UpdateTicket/{id}")
    public ResponseEntity<String> updateTicket(@PathVariable Long id, @Valid @RequestBody TicketDTO ticketDTO) {
        try {
            TicketDTO updatedTicket = acceso.updateTicket(id, ticketDTO);
            return new ResponseEntity<>(updatedTicket.toString(), HttpStatus.OK);

        } catch (ExceptionTicketBadRequest e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors.toString(), HttpStatus.BAD_REQUEST);
        }catch (ExceptionTicketNotFound e){
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors.toString(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            e.printStackTrace();
            errors.put("error", "Ocurrió un error interno del servidor al actualizar el ticket.");
            return new ResponseEntity<>(errors.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @DeleteMapping("/DeleteTicket/{id}")
    public ResponseEntity<String> deleteTicket(@PathVariable Long id) {
        try {
            acceso.deleteTicket(id);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (ExceptionTicketBadRequest e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors.toString(), HttpStatus.BAD_REQUEST);
        }catch (ExceptionTicketNotFound e){
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors.toString(), HttpStatus.NOT_FOUND);
        }catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", "Ocurrió un error interno del servidor al intentar eliminar el ticket.");
            return new ResponseEntity<>(errors.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
