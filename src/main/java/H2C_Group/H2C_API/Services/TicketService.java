package H2C_Group.H2C_API.Services;


import H2C_Group.H2C_API.Entities.TicketEntity;
import H2C_Group.H2C_API.Entities.UserEntity;
import H2C_Group.H2C_API.Enums.*;
import H2C_Group.H2C_API.Exceptions.ExceptionTicketNotFound;
import H2C_Group.H2C_API.Exceptions.ExceptionUserNotFound;
import H2C_Group.H2C_API.Models.DTO.*;
import H2C_Group.H2C_API.Repositories.TicketRepository;
import H2C_Group.H2C_API.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TicketService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;


    public Page<TicketDTO> getAllTickets(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TicketEntity> tickets = ticketRepository.findAll(pageable);
        return tickets.map(this::convertToTicketDTO);
    }

    public List<TicketDTO> geTicketByUserId(Long userId){
        userRepository.findById(userId).orElseThrow(() -> new ExceptionUserNotFound("El id del usuario " + " no existe" ));
        List<TicketEntity> tickets = ticketRepository.findByUserCreator_UserIdOrderByCreationDate(userId);
        return tickets.stream()
                .map(this::convertToTicketDTO)
                .collect(Collectors.toList());
    }

    public TicketDTO getTicketById(Long id) {
        TicketEntity ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ExceptionTicketNotFound("Ticket con ID " + id + " no encontrado."));
        return convertToTicketDTO(ticket);
    }

    public TicketDTO createTicket(TicketDTO ticketDTO) {

        //Validaciones
        userRepository.findById(ticketDTO.getUserId()).orElseThrow(() -> new IllegalArgumentException("El ID del usuario " + ticketDTO.getUserId() + " no existe"));

        //Si se proporciona un id de tecnico (en caso de haber tomado un ticket como tecnico), verificar si existe el usuario
        if (ticketDTO.getAssignedTech() != null){
            userRepository.findById(ticketDTO.getAssignedTech().getId()).orElseThrow(() ->  new IllegalArgumentException("El ID del usuario " + ticketDTO.getUserId() + " no existe"));
        }


        //Creacion (Insercion) de Ticket --

        //Conversion DTO -> Entity
        TicketEntity ticketEntity = new TicketEntity();

        //Asignacion del procentaje
        ticketEntity.setPercentage(ticketDTO.getPercentage());

        //Asignacion de url de imagen del ticket
        ticketEntity.setImageUrl(ticketDTO.getImageUrl());

        ticketEntity.setTicketId(ticketDTO.getTicketId());
        //Asignacion de categoria
        Category category = Category.fromId(ticketDTO.getCategory().getId()).orElseThrow(() -> new IllegalArgumentException("La categoria de id " + ticketDTO.getCategory().getId() + "  no existe."));
        ticketEntity.setCategoryId(ticketDTO.getCategory().getId());

        //Asignacion de prioridad
        TicketPriority priority = TicketPriority.fromId(ticketDTO.getPriority().getId()).orElseThrow(() -> new IllegalArgumentException("El id de priodidad " +  ticketDTO.getPriority().getId() + "  no existe."));
        ticketEntity.setPriorityId(ticketDTO.getPriority().getId());

        //Asignacion de estado para ticket. Por defecto, el estado de creacion es "En espera"
        ticketEntity.setTicketStatusId(TicketStatus.EN_ESPERA.getId());

        UserEntity creatorUser = userRepository.findById(ticketDTO.getUserId()).orElseThrow(() -> new IllegalArgumentException("El usuario (cliente) con id " + ticketDTO.getUserId() + " no existe."));
        ticketEntity.setUserCreator(creatorUser);

        ticketEntity.setTitle(ticketDTO.getTitle());
        ticketEntity.setDescription(ticketDTO.getDescription());

        ticketEntity.setAssignedTechUser(null); //Al crear un ticket, este estara en estado de espera, por lo tanto, no tendra un tecnico asignado


        ticketEntity.setCreationDate(ticketDTO.getCreationDate());

        //Asignacion de fecha de cierre como NULL al crearse. La fecha se asignara cuando se cierre la solicitud (es decir, ticketStatus = "Cerrado")
        ticketEntity.setCloseDate(null);

        //Asignacion del procentaje
        ticketEntity.setPercentage(ticketDTO.getPercentage());

        //Almacenamiento de ticket creado en la DB
        TicketEntity savedTicket = ticketRepository.save(ticketEntity);

        //Notificación para el cliente
        String notificationMessage = "Tu ticket #" + savedTicket.getTicketId() + " - " + savedTicket.getTitle() + " ha sido creado exitosamente.";
        String userId = String.valueOf(savedTicket.getUserCreator().getUserId()); //Obtiene el ID del usuario creador del ticket

        messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", notificationMessage);

        //Conversion del ticket almacenado de vuelta a DTO para la respuesta del Frontend
        return  convertToTicketDTO(savedTicket);
    }

    public TicketDTO updateTicketStatus(Long id, TicketStatusDTO ticketDTO) {
        // 1. Encuentra el ticket existente o lanza una excepción si no existe.
        TicketEntity existingTicket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El ID del ticket " + id + " no existe."));

        // 2. Procesa la actualización del estado si el DTO no es nulo.
        if (ticketDTO != null && ticketDTO.getId() != null) {
            TicketStatus statusEnum = TicketStatus.fromIdOptional(ticketDTO.getId())
                    .orElseThrow(() -> new IllegalArgumentException("El ID de estado proporcionado no existe en la enumeración de TicketStatus: " + ticketDTO.getId()));

            if (ticketDTO.getDisplayName() != null && !statusEnum.getDisplayName().equalsIgnoreCase(ticketDTO.getDisplayName())) {
                throw new IllegalArgumentException("El 'displayName' del estado no coincide con el 'id' proporcionado. Se esperaba: '" + statusEnum.getDisplayName() + "'");
            }

            existingTicket.setTicketStatusId(statusEnum.getId());

            if (statusEnum.equals(TicketStatus.COMPLETADO)) {
                existingTicket.setCloseDate(LocalDateTime.now());
            } else if (existingTicket.getCloseDate() != null) {
                existingTicket.setCloseDate(null);
            }
        }

        // 3. Guarda la entidad actualizada y conviértela a DTO.
        TicketEntity savedTicket = ticketRepository.save(existingTicket);
        return convertToTicketDTO(savedTicket);
    }




    public TicketDTO updateTicket(Long id, TicketDTO ticketDTO) {

        // Validaciones básicas de entrada
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID del ticket a actualizar no puede ser nulo o no válido.");
        }

        TicketEntity existingTicket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El ID del ticket " + id + " no existe."));

        // --- Actualización de TÍTULO ---
        if (ticketDTO.getTitle() != null) { // Solo actualiza si se provee un nuevo título
            existingTicket.setTitle(ticketDTO.getTitle());
        }

        // --- Actualización de DESCRIPCIÓN ---
        if (ticketDTO.getDescription() != null) { // Solo actualiza si se provee una nueva descripción
            existingTicket.setDescription(ticketDTO.getDescription());
        }

        // --- Actualización de TÉCNICO ASIGNADO (assignedTechUser) ---
        // ticketDTO.getAssignedTech() en el DTO es el Long ID del técnico
        if (ticketDTO.getAssignedTech() != null) {
            UserEntity userTech = userRepository.findById(ticketDTO.getAssignedTech().getId()).orElseThrow(() -> new IllegalArgumentException("El ID del técnico asignado " + ticketDTO.getAssignedTech() + " no existe."));

            Long userRoleId = userTech.getRolId();

            // Validar si el rol es ADMINISTRADOR o TECNICO
            if (userRoleId.equals(UserRole.ADMINISTRADOR.getId()) || userRoleId.equals(UserRole.TECNICO.getId())) {
                //Notificación para el técnico
                if (existingTicket.getAssignedTechUser() == null || !existingTicket.getAssignedTechUser().getUserId().equals(userTech.getUserId())) {
                    //Solo se envía la notificación si el técnico es nuevo
                    String notificationMessage = "Se te ha asignado el ticket #" + existingTicket.getTicketId() + " - " + existingTicket.getTitle();
                    String techId = String.valueOf(userTech.getUserId());
                    messagingTemplate.convertAndSendToUser(techId, "/queue/notifications", notificationMessage);
                }
                existingTicket.setAssignedTechUser(userTech);
            } else {
                throw new IllegalArgumentException("El usuario con ID " + userTech.getUserId() + " no tiene un rol válido para ser asignado como técnico (debe ser Administrador o Técnico).");
            }
        }

        // --- Actualización de ESTADO (TicketStatus) ---
        if (ticketDTO.getStatus() != null) {
            TicketStatusDTO ticketStatusDTO = ticketDTO.getStatus();

            if (ticketStatusDTO.getId() == null) { // Ya no es necesario validar display name si fromIdOptional lo hace
                throw new IllegalArgumentException("El ID de estado es obligatorio.");
            }

            TicketStatus statusEnum = TicketStatus.fromIdOptional(ticketStatusDTO.getId())
                    .orElseThrow(() -> new IllegalArgumentException("El ID de estado proporcionado no existe en la enumeración de TicketStatus: " + ticketStatusDTO.getId()));

            // VALIDACION CRITICA: Verifica si el displayName coincide
            if (!statusEnum.getDisplayName().equalsIgnoreCase(ticketStatusDTO.getDisplayName())) {
                throw new IllegalArgumentException("El 'displayName' del estado ('" + ticketStatusDTO.getDisplayName() + "') no coincide con el 'id' proporcionado (" + ticketStatusDTO.getId() + "). Se esperaba: '" + statusEnum.getDisplayName() + "'");
            }

            existingTicket.setTicketStatusId(statusEnum.getId());

            // Validacion: Si el estado cambia a "Completado", establecer closeDate automáticamente
            if (statusEnum.equals(TicketStatus.COMPLETADO)) {
                existingTicket.setCloseDate(LocalDateTime.now());
            } else if (existingTicket.getCloseDate() != null) {
                // Si el estado cambia de "Cerrado" a otro, eliminar closeDate
                existingTicket.setCloseDate(null);
            }
        }


        // --- Actualización de CATEGORÍA ---
        if (ticketDTO.getCategory() != null) {
            CategoryDTO categoryFromDTO = ticketDTO.getCategory();
            if (categoryFromDTO.getId() == null) { // Validación de ID obligatoria
                throw new IllegalArgumentException("El ID de categoría es obligatorio.");
            }
            Category categoryEnum = Category.fromIdOptional(categoryFromDTO.getId())
                    .orElseThrow(() -> new IllegalArgumentException("El ID de categoría proporcionado no existe en la enumeración de Categoría: " + categoryFromDTO.getId()));

            if (!categoryEnum.getDisplayName().equalsIgnoreCase(categoryFromDTO.getDisplayName())) {
                throw new IllegalArgumentException("El 'displayName' de la categoría ('" + categoryFromDTO.getDisplayName() + "') no coincide con el 'id' proporcionado (" + categoryFromDTO.getId() + "). Se esperaba: '" + categoryEnum.getDisplayName() + "'");
            }

            existingTicket.setCategoryId(categoryEnum.getId());
        }

        // --- Actualización de PRIORIDAD ---
        if (ticketDTO.getPriority() != null) {
            TicketPriorityDTO priorityFromDTO = ticketDTO.getPriority();
            if (priorityFromDTO.getId() == null) { // Validación de ID obligatoria
                throw new IllegalArgumentException("El ID de prioridad es obligatorio.");
            }
            TicketPriority priorityEnum = TicketPriority.fromIdOptional(priorityFromDTO.getId()).orElseThrow(() -> new IllegalArgumentException("El ID de prioridad proporcionado no existe en la enumeración de Prioridad: " + priorityFromDTO.getId()));

            if (!priorityEnum.getDisplayName().equalsIgnoreCase(priorityFromDTO.getDisplayName())) {
                throw new IllegalArgumentException("El 'displayName' de la prioridad ('" + priorityFromDTO.getDisplayName() + "') no coincide con el 'id' proporcionado (" + priorityFromDTO.getId() + "). Se esperaba: '" + priorityEnum.getDisplayName() + "'");
            }


//            // Ejemplo si el DTO incluye el ID del usuario que realiza la acción:
//            if (ticketDTO.getUserId() == null) {
//                throw new IllegalArgumentException("El ID del usuario que realiza la acción es requerido para actualizar la prioridad.");
//            }
//            UserEntity performingUser = userRepository.findById(ticketDTO.getUserId()).orElseThrow(() -> new IllegalArgumentException("El usuario que realiza la acción con ID " + ticketDTO.getUserId() + " no existe."));
//
//
//            if (performingUser.getRolId() == null || !performingUser.getRolId().equals(UserRole.TECNICO.getId()) && !performingUser.getRolId().equals(UserRole.ADMINISTRADOR.getId())) { // Si el usuario no es un admin o tecnico, no puede actualizar
//                throw new IllegalArgumentException("Solo técnicos y administradores pueden actualizar la prioridad del ticket.");
//            }
            existingTicket.setPriorityId(priorityEnum.getId());
        }

        if (ticketDTO.getCloseDate() != null) {
            existingTicket.setCloseDate(ticketDTO.getCloseDate());
        }

        if (ticketDTO.getPercentage() !=null){
            existingTicket.setPercentage(ticketDTO.getPercentage());
        }

        if(ticketDTO.getImageUrl() !=null){
            existingTicket.setImageUrl(ticketDTO.getImageUrl());
        }

        TicketEntity savedTicket = ticketRepository.save(existingTicket);
        return convertToTicketDTO(savedTicket);

    }


    //EL METODO DELETE SERA DISCUTIDO, PUEDE QUE NO SEA NECESARIO -------------------------------------------------------
    public void deleteTicket(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID del ticket no puede ser nulo o no válido");
        }

        boolean exists = ticketRepository.existsById(id);

        if (!exists) {
            throw new ExceptionTicketNotFound("Ticket con ID " + id + " no encontrado.");
        }

        ticketRepository.deleteById(id);
    }



    private TicketDTO convertToTicketDTO(TicketEntity ticket) {
        TicketDTO dto = new TicketDTO();
        dto.setTicketId(ticket.getTicketId());

        Category categoryEnum = Category.fromIdOptional(ticket.getCategoryId()).orElseThrow(() -> new IllegalArgumentException("ID de categoría inválido al convertir a DTO: " + ticket.getCategoryId()));
        dto.setCategory(new CategoryDTO(categoryEnum.getId(), categoryEnum.getDisplayName())); // <-- Aquí se crea el CategoryDTO dto.setPriority((TicketPriority.fromId(ticket.getPriorityId())).orElseThrow(() -> new IllegalArgumentException("ID de prioridad inválido: " + ticket.getPriorityId())));

        TicketPriority priorityEnum = TicketPriority.fromIdOptional(ticket.getPriorityId()).orElseThrow(() -> new IllegalArgumentException("ID de prioridad invalido al convertir DTO: " + ticket.getPriorityId()));
        dto.setPriority(new TicketPriorityDTO(priorityEnum.getId(), priorityEnum.getDisplayName()));

        TicketStatus statusEnum = TicketStatus.fromIdOptional(ticket.getTicketStatusId()).orElseThrow(() -> new IllegalArgumentException("ID de estado invalido al convertir DTO:  " + ticket.getTicketStatusId()));
        dto.setStatus(new TicketStatusDTO(statusEnum.getId(), statusEnum.getDisplayName()));

        if (ticket.getUserCreator() != null) {

            Long userCreatorId = ticket.getUserCreator().getUserId(); //Declaracion de variable para almacenamiento del id de tipo Long

            dto.setUserId(userCreatorId); //Asignacion de ID del usuario creador del ticket


            userRepository.findById(userCreatorId).ifPresent(user -> {dto.setUserName(user.getFullName());}); //Asignacion de nombre completo para campo userName en TicketDTO
        } else {
            throw new IllegalArgumentException("El ID del usuario no puede ser nulo.");
        }


        dto.setTitle(ticket.getTitle());
        dto.setDescription(ticket.getDescription());

        if (ticket.getAssignedTechUser() != null) {
            UserDTO techDTO = new UserDTO();
            techDTO.setId(ticket.getAssignedTechUser().getUserId());
            techDTO.setDisplayName(ticket.getAssignedTechUser().getFullName());
            dto.setAssignedTech(techDTO);
        } else {
            dto.setAssignedTech(null); // Establece el ID del técnico a null en el DTO si no hay técnico asignado
        }
        dto.setCreationDate(ticket.getCreationDate());

        dto.setCloseDate(ticket.getCloseDate());

        dto.setPercentage(ticket.getPercentage());

        dto.setImageUrl(ticket.getImageUrl());
        return dto;

    }

    public Map<String, Long> getTicketCountsByStatus() {
        List<Object[]> results = ticketRepository.countTicketsByStatus();
        Map<String, Long> counts = new HashMap<>();
        for (Object[] result : results) {
            String statusName = (String) result[0];
            // Castea a BigDecimal y luego usa longValue() para obtener el Long
            Long count = ((java.math.BigDecimal) result[1]).longValue();
            counts.put(statusName, count);
        }
        return counts;
    }

    public List<TicketDTO> getAssignedTicketsByTechnicianId(Long technicianId) {
        userRepository.findById(technicianId)
                .orElseThrow(() -> new ExceptionUserNotFound("El id del tecnico " + technicianId + " no existe"));

        List<TicketEntity> tickets = ticketRepository.findByAssignedTechUser_UserId(technicianId);
        return tickets.stream()
                .map(this::convertToTicketDTO)
                .collect(Collectors.toList());
    }

    public TicketDTO acceptTicket(Long ticketId, Long technicianId) {
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket con ID " + ticketId + " no encontrado."));

        // Validar que el ticket tiene un técnico asignado y es el que está intentando aceptarlo
        if (ticket.getAssignedTechUser() == null || !ticket.getAssignedTechUser().getUserId().equals(technicianId)) {
            throw new IllegalArgumentException("El usuario no tiene permiso para aceptar este ticket.");
        }

        // Validar que el ticket está en estado "En espera" antes de cambiarlo
        if (!ticket.getTicketStatusId().equals(TicketStatus.EN_ESPERA.getId())) {
            throw new IllegalArgumentException("El ticket no puede ser aceptado, su estado actual no es 'En espera'.");
        }

        // Cambiar el estado a "En progreso"
        ticket.setTicketStatusId(TicketStatus.EN_PROGRESO.getId());

        TicketEntity savedTicket = ticketRepository.save(ticket);

        //Notificación para el técnico
        String notificationMessage = "Has aceptado el ticket #" + savedTicket.getTicketId() + " - " + savedTicket.getTitle();
        String techId = String.valueOf(technicianId);
        messagingTemplate.convertAndSendToUser(techId, "/queue/notifications", notificationMessage);

        return convertToTicketDTO(savedTicket);
    }

    public Long countByUserId(Long userId) {
        return ticketRepository.countTicketsByUserId(userId);
    }
}