package H2C_Group.H2C_API.Controllers;

import H2C_Group.H2C_API.Exceptions.ExceptionTicketNotFound;
import H2C_Group.H2C_API.Exceptions.ExceptionUserNotFound;
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
    public ResponseEntity<Page<TicketDTO>> getTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        if(size <= 0 || size > 50){
            ResponseEntity.badRequest().body(Map.of("status", "el tamaño de la página debe estar entre 1 y 50"));
            return ResponseEntity.ok(null);
        }
        Page<TicketDTO> tickets = acceso.getAllTickets(page, size);
        if (tickets == null) {
            ResponseEntity.badRequest().body(Map.of("status", "No hay tickets registrados."));
        }
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/GetTicketById/{id}")
    public ResponseEntity<TicketDTO> getTicketById(@PathVariable Long id){
        try {
            TicketDTO ticket = acceso.getTicketById(id);
            return new ResponseEntity<>(ticket, HttpStatus.OK);
        } catch (ExceptionTicketNotFound e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/GetRecentTicketsByUser/{userId}")
    public ResponseEntity<List<TicketDTO>> getTicketsByUserId(@PathVariable Long userId) {
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
        }catch (IllegalArgumentException e) {
            //Validación de argumentos invalidos
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST); // Código 400
        }catch (ExceptionUserNotFound e) {
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


    @PatchMapping("/UpdateTicket/{ticketId}")
    public ResponseEntity<?> updateTicket(@PathVariable Long ticketId, @Valid @RequestBody TicketDTO payload) {
        try {
            TicketDTO updatedTicket = acceso.updateTicket(ticketId, payload);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        } catch (ExceptionTicketNotFound e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", "Ocurrió un error interno del servidor al actualizar el ticket.");
            return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @DeleteMapping("/DeleteTicket/{id}")
    public ResponseEntity<?> deleteTicket(@PathVariable Long id) {
        try {
            acceso.deleteTicket(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        } catch (ExceptionTicketNotFound e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", "Ocurrió un error interno del servidor al intentar eliminar el ticket.");
            return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
