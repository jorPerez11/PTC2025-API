package H2C_Group.H2C_API.Controllers;

import H2C_Group.H2C_API.Exceptions.TicketExceptions;
import H2C_Group.H2C_API.Exceptions.UserExceptions;
import H2C_Group.H2C_API.Models.DTO.TicketDTO;
import H2C_Group.H2C_API.Services.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public List<TicketDTO> getTickets() {
        return acceso.getAllTickets();
    }

    @PostMapping("/PostTicket")
    public ResponseEntity<?> postTicket(@RequestBody TicketDTO ticketDTO) {
        try {
            TicketDTO newTicket = acceso.createTicket(ticketDTO);
            return new ResponseEntity<>(newTicket, HttpStatus.CREATED);
        }catch (IllegalArgumentException e) {
            //Validación de argumentos invalidos
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST); // Código 400
        }catch (UserExceptions.UserNotFoundException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND); // Código 404
        }catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", "Ocurrió un error interno del servidor al crear el ticket.");
            return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR); // Código 500
        }

    }


    @PatchMapping("/UpdateTicket/{id}")
    public ResponseEntity<String> updateTicket(@PathVariable Long id, @RequestBody TicketDTO ticketDTO) {
        try {
            TicketDTO updatedTicket = acceso.updateTicket(id, ticketDTO);
            return new ResponseEntity<>(updatedTicket.toString(), HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors.toString(), HttpStatus.BAD_REQUEST);

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
        }catch (IllegalArgumentException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors.toString(), HttpStatus.BAD_REQUEST);
        }catch (TicketExceptions.TicketNotFoundException e){
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
