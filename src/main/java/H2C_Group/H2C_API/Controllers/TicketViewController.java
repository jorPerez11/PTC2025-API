package H2C_Group.H2C_API.Controllers;

import H2C_Group.H2C_API.Entities.Views.TicketDetailView;
import H2C_Group.H2C_API.Entities.Views.TicketView;
import H2C_Group.H2C_API.Exceptions.ExceptionTicketNotFound;
import H2C_Group.H2C_API.Models.DTO.TicketDTO;
import H2C_Group.H2C_API.Models.DTO.TicketViewDTO;
import H2C_Group.H2C_API.Services.TicketService;
import H2C_Group.H2C_API.Services.TicketViewService;
import H2C_Group.H2C_API.Services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/tickets")
public class TicketViewController {

    @Autowired
    private TicketService acceso;

    @Autowired
    private TicketViewService ticketViewService;

    @Autowired
    private UserService userService;

    // Nuevo endpoint para obtener los tickets asignados al técnico autenticado
    @GetMapping("/myTickets")
    public List<TicketViewDTO> getTicketsForAssignedTech() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Obtener el objeto principal, que es de tipo UserDetails (por defecto de Spring Security)
        Object principal = authentication.getPrincipal();

        // Verificar si el principal es una instancia de UserDetails
        if (principal instanceof UserDetails) {
            // Extraer el nombre de usuario
            String username = ((UserDetails) principal).getUsername();

            // Buscar el ID del usuario en tu base de datos usando el nombre de usuario
            // Asumo que tu UserService tiene un método para esto.
            Long techId = userService.getUserIdByUsername(username);

            // Obtener los tickets usando el ID de tu base de datos
            return ticketViewService.getTicketsByAssignedTech(techId);
        } else {
            // Manejar el caso donde no hay usuario autenticado o no es del tipo esperado
            throw new IllegalStateException("Principal no es de tipo UserDetails.");
        }
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketDetailView> getTicketDetails(@PathVariable Long ticketId) {
        Optional<TicketDetailView> ticketDetails = ticketViewService.getTicketDetails(ticketId);
        return ticketDetails.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/getAll")
    public List<TicketView> getAllTickets() {
        return ticketViewService.getAllTickets();
    }

    @PatchMapping("/updateStatus/{ticketId}")
    public ResponseEntity<?> updateTicket(@PathVariable Long ticketId, @Valid @RequestBody TicketDTO payload) {
        try {
            TicketDTO updatedTicket = acceso.UpdateTicketStatus(ticketId, payload);
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
}
