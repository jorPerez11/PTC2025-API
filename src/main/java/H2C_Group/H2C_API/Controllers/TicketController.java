package H2C_Group.H2C_API.Controllers;

import H2C_Group.H2C_API.Exceptions.ExceptionTicketNotFound;
import H2C_Group.H2C_API.Exceptions.ExceptionUserNotFound;
import H2C_Group.H2C_API.Models.DTO.PagedResponseDTO;
import H2C_Group.H2C_API.Models.DTO.TicketDTO;
import H2C_Group.H2C_API.Models.DTO.TicketStatusDTO;
import H2C_Group.H2C_API.Services.TicketService;
import H2C_Group.H2C_API.Services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class TicketController {
    @Autowired
    private TicketService acceso;

    @Autowired
    private UserService userService;

    // Nuevo endpoint para obtener el conteo de tickets por estado
    @GetMapping("/admin/GetTicketCounts")
    public ResponseEntity<?> getTicketCounts() {
        try {
            System.out.println("Solicitud recibida para /GetTicketCounts");
            Map<String, Long> counts = acceso.getTicketCountsByStatus();
            System.out.println("Resultados de la consulta: " + counts);
            return new ResponseEntity<>(counts, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("🔥 ERROR DE EJECUCIÓN: " + e.getMessage());
            System.out.println("Error al procesar la solicitud: " + e.getMessage());
            e.printStackTrace(); // Imprime el stack trace completo
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }

    @GetMapping("/admin/GetTickets")
    public ResponseEntity<PagedResponseDTO<TicketDTO>> getTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        if(size <= 0 || size > 50){
            ResponseEntity.badRequest().body(Map.of("status", "el tamaño de la página debe estar entre 1 y 50"));
            return ResponseEntity.badRequest().body(null);
        }
        Page<TicketDTO> tickets = acceso.getAllTickets(page, size);
        if (tickets == null) {
            ResponseEntity.badRequest().body(Map.of("status", "No hay tickets registrados."));
        }

        try{
            Page<TicketDTO> ticketPage = acceso.getAllTickets(page, size);

            PagedResponseDTO<TicketDTO> response = new PagedResponseDTO<>();
            response.setContent(ticketPage.getContent());

            response.setTotalElements(ticketPage.getTotalElements());
            response.setTotalPages(ticketPage.getTotalPages());
            response.setNumber(ticketPage.getNumber());
            response.setSize(ticketPage.getSize());

            if (ticketPage.isEmpty()) {
                // Si no hay tickets, devolver 200 OK con el cuerpo de paginación vacío
                // No es necesario devolver 400 Bad Request aquí.
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.ok(response);

        }catch(ExceptionTicketNotFound e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }


    }

    @GetMapping("/client/GetTicketById/{id}")
    public ResponseEntity<TicketDTO> getTicketById(@PathVariable Long id){
        try {
            TicketDTO ticket = acceso.getTicketById(id);
            return new ResponseEntity<>(ticket, HttpStatus.OK);
        } catch (ExceptionTicketNotFound e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/tech/GetTicketsEnEspera")
    public ResponseEntity<?> getTicketsEnEspera(){
        try {
            List<TicketDTO> tickets = acceso.getTicketsEnespera();
            if (tickets.isEmpty()){
                return ResponseEntity.noContent().build();
            }
            return new ResponseEntity<>(tickets, HttpStatus.OK);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error al obtener la lista de tickets en espera"
                    ));
        }
    }


    @PatchMapping("/admin/UpdateTicketStatus/{ticketId}")
    public ResponseEntity<?> updateTicketStatus(@PathVariable Long ticketId, @RequestBody TicketStatusDTO ticketDTO) {
        try {
            TicketDTO updatedTicket = acceso.updateTicketStatus(ticketId, ticketDTO);
            return new ResponseEntity<>(updatedTicket, HttpStatus.OK);
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


    @GetMapping("/client/GetRecentTicketsByUser/{userId}")
    public ResponseEntity<List<TicketDTO>> getTicketsByUserId(@PathVariable Long userId) {
        List<TicketDTO> tickets = acceso.geTicketByUserId(userId);
        return new ResponseEntity<>(tickets, HttpStatus.OK);
    }

    @GetMapping("/tech/GetAssignedTicketsByTech/{technicianId}")
    public ResponseEntity<List<TicketDTO>> getAssignedTicketsByTechnicianId(@PathVariable Long technicianId){
        List<TicketDTO> tickets = acceso.getAssignedTicketsByTechnicianId(technicianId);
        return new ResponseEntity<>(tickets, HttpStatus.OK);
    }

    @GetMapping("/tech/getAssignedTicketsByTechnicianIdPage/{technicianId}")
    public ResponseEntity<Page<TicketDTO>> getAssignedTicketsByTechnicianIdPage(
            @PathVariable Long technicianId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        // 1. Obtener la identidad del usuario que está logueado (desde el JWT/sesión)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Obtener el ID del usuario logueado usando el username del token.
        // Esto requiere el metodo en el UserService (ver Sección 3).
        Long idUsuarioLogueado = userService.getUserIdByUsername(authentication.getName());

        // 2. Verificar si el usuario logueado es Administrador
        boolean esAdmin = authentication.getAuthorities().stream()
                // Usa el string de rol sin el acento para asegurar la coincidencia
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));

        // 3. 🛑 VALIDACIÓN DE PROPIEDAD Y ROL
        // Denegar el acceso si NO es Admin Y el ID solicitado NO es el ID propio.
        if (!esAdmin && !technicianId.equals(idUsuarioLogueado)) {
            // Lanza 403 Forbidden si el usuario está intentando ver datos de otro técnico.
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        // 4. Si la validación pasa, ejecuta la lógica de negocio.
        Page<TicketDTO> ticketPage = acceso.getAssignedTicketsByTechnicianIdPage(technicianId, page, size);
        return new ResponseEntity<>(ticketPage, HttpStatus.OK);
    }


    @PutMapping("/tech/accept/{ticketId}/{technicianId}")
    public ResponseEntity<TicketDTO> acceptTicket(@PathVariable Long ticketId, @PathVariable Long technicianId) {
        TicketDTO acceptedTicket = acceso.acceptTicket(ticketId, technicianId);
        return new ResponseEntity<>(acceptedTicket, HttpStatus.OK);
    }


    @PostMapping("/client/PostTicket")
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


    @PatchMapping("/client/UpdateTicket/{ticketId}")
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


    @DeleteMapping("/client/DeleteTicket/{id}")
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

    @GetMapping("/tech/available-tickets")
    public ResponseEntity<?> getAvailableTicketsForTechnician(@RequestParam("technicianId") Long technicianId) {
        try {
            List<TicketDTO> tickets = acceso.getAvailableTicketsForTechnician(technicianId);
            return new ResponseEntity<>(tickets, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener los tickets disponibles."));
        }
    }

    @PostMapping("/tech/decline-ticket/{ticketId}/{technicianId}")
    public ResponseEntity<?> declineTicket(@PathVariable Long ticketId, @PathVariable Long technicianId) {
        try {
            acceso.declineTicket(ticketId, technicianId);
            return ResponseEntity.ok(Map.of("message", "Ticket declinado exitosamente."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al declinar el ticket."));
        }
    }

    @GetMapping("/client/count/by-user/{userId}")
    public ResponseEntity<Long> getTicketCountByUser(@PathVariable Long userId) {
        Long count = acceso.countByUserId(userId);
        return ResponseEntity.ok(count);
    }

    @PatchMapping("/tech/UpdateTicketProgress/{ticketId}")
    public ResponseEntity<?> updateTicketProgress(@PathVariable Long ticketId, @RequestBody TicketDTO payload) {
        try {
            TicketDTO updatedTicket = acceso.updateTicket(ticketId, payload);
            return ResponseEntity.ok(updatedTicket);
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
            errors.put("error", "Ocurrió un error interno del servidor al actualizar el progreso del ticket.");
            return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/tech/FinalizeTicket/{ticketId}")
    public ResponseEntity<?> finalizeTicket(@PathVariable Long ticketId, @RequestBody TicketDTO payload) {
        try {
            // Asigna el estado "Completado" y el 100% de progreso
            payload.setPercentage(100);
            payload.setStatus(new TicketStatusDTO(3L, "Completado"));

            TicketDTO updatedTicket = acceso.updateTicket(ticketId, payload);
            return ResponseEntity.ok(updatedTicket);
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
            errors.put("error", "Ocurrió un error interno del servidor al finalizar el ticket.");
            return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/tech/count/completed-by-tech/{techId}")
    public ResponseEntity<Long> getCompletedTicketCountByTech(@PathVariable Long techId) {
        Long count = acceso.countCompletedByTechId(techId);
        return ResponseEntity.ok(count);
    }

}

