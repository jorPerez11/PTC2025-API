package H2C_Group.H2C_API.Services;


import H2C_Group.H2C_API.Entities.DeclinedTicketEntity;
import H2C_Group.H2C_API.Entities.TicketEntity;
import H2C_Group.H2C_API.Entities.TicketStatusEntity;
import H2C_Group.H2C_API.Entities.UserEntity;
import H2C_Group.H2C_API.Enums.*;
import H2C_Group.H2C_API.Exceptions.ExceptionTicketNotFound;
import H2C_Group.H2C_API.Exceptions.ExceptionUserNotFound;
import H2C_Group.H2C_API.Models.DTO.*;
import H2C_Group.H2C_API.Repositories.DeclinedTicketRepository;
import H2C_Group.H2C_API.Repositories.TicketRepository;
import H2C_Group.H2C_API.Repositories.TicketStatusRepository;
import H2C_Group.H2C_API.Repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TicketService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
  
  @Autowired
    private TicketStatusRepository ticketStatusRepository;


    @Autowired
    private DeclinedTicketRepository declinedTicketRepository;


    public Page<TicketDTO> getAllTickets(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        //Page<TicketEntity> tickets = ticketRepository.findAll(pageable);
        Page<TicketEntity> tickets = ticketRepository.findAllWithUsers(pageable);
        return tickets.map(this::convertToTicketDTO);
    }

//    public List<TicketDTO> getAllTicketsAsList(int page, int size) {
//        // Reutiliza la lógica de tu servicio para obtener la página
//        Page<TicketDTO> ticketPage = this.getAllTickets(page, size);
//
//        // Devuelve solo el contenido (la lista de DTOs)
//        return ticketPage.getContent();
//    }

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


    //Obtiene los tickets disponibles para un tecnico (mientras estan en espera y no rechazados)
    public List<TicketDTO> getAvailableTicketsForTechnician(Long technicianId) {
        // 1. Obtener los IDs de los tickets que el técnico ha rechazado.
        List<DeclinedTicketEntity> declinedTickets = declinedTicketRepository.findByTechnicianId(technicianId);
        Set<Long> declinedTicketIds = declinedTickets.stream()
                .map(DeclinedTicketEntity::getTicketId)
                .collect(Collectors.toSet()); // Usamos un Set para un acceso más rápido

        // 2. Buscar tickets en estado "En espera".
        Long enEsperaId = TicketStatus.EN_ESPERA.getId();
        List<TicketEntity> allEnEsperaTickets = ticketRepository.findByTicketStatusId(enEsperaId);

        // 3. Filtrar los tickets rechazados.
        List<TicketEntity> availableTickets = allEnEsperaTickets.stream()
                .filter(ticket -> !declinedTicketIds.contains(ticket.getTicketId()))
                .collect(Collectors.toList());

        // 4. Convertir a DTO y devolver.
        return availableTickets.stream()
                .map(this::convertToTicketDTO)
                .collect(Collectors.toList());
    }


    public TicketDTO createTicket(TicketDTO ticketDTO) {

        // ✅ LÓGICA CORREGIDA: Primero, validamos y asignamos al usuario creador
        UserEntity creatorUser = userRepository.findById(ticketDTO.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("El usuario (cliente) con id " + ticketDTO.getUserId() + " no existe."));

        // Creación e inicialización de la entidad
        TicketEntity ticketEntity = new TicketEntity();
        ticketEntity.setPercentage(ticketDTO.getPercentage());
        ticketEntity.setImageUrl(ticketDTO.getImageUrl());
        ticketEntity.setTitle(ticketDTO.getTitle());
        ticketEntity.setDescription(ticketDTO.getDescription());
        ticketEntity.setUserCreator(creatorUser); // Asignación del usuario creador

        // Asignación de la categoría
        Category category = Category.fromId(ticketDTO.getCategory().getId())
                .orElseThrow(() -> new IllegalArgumentException("La categoria de id " + ticketDTO.getCategory().getId() + "  no existe."));
        ticketEntity.setCategoryId(category.getId());

        // Asignación de la prioridad
        TicketPriority priority = TicketPriority.fromId(ticketDTO.getPriority().getId())
                .orElseThrow(() -> new IllegalArgumentException("El id de priodidad " +  ticketDTO.getPriority().getId() + "  no existe."));
        ticketEntity.setPriorityId(priority.getId());

        ticketEntity.setTicketStatusId(TicketStatus.EN_ESPERA.getId());


            ticketEntity.setAssignedTechUser(null);
            ticketEntity.setCreationDate(ticketDTO.getCreationDate());
            ticketEntity.setCloseDate(null);



        // Almacenamiento de ticket creado en la DB
        TicketEntity savedTicket = ticketRepository.save(ticketEntity);

        // Notificación para el cliente
        String notificationMessage = "Tu ticket #" + savedTicket.getTicketId() + " - " + savedTicket.getTitle() + " ha sido creado exitosamente.";
        String username = savedTicket.getUserCreator().getUsername();
        System.out.println("📤 Enviando notificación a: " + username); // Log para confirmar
        messagingTemplate.convertAndSendToUser(username, "/queue/notifications", notificationMessage);

        // Conversión del ticket almacenado de vuelta a DTO para la respuesta del Frontend
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

    @Transactional
    public TicketDTO UpdateTicketStatus(Long ticketId, TicketDTO ticketDTO) {
        // Usar el repositorio para encontrar el ticket.
        Optional<TicketEntity> optionalTicket = ticketRepository.findById(ticketId);

        if (optionalTicket.isPresent()) {
            TicketEntity ticket = optionalTicket.get();

            // Asegúrate de que el DTO contenga un estado válido
            if (ticketDTO.getStatus() == null || ticketDTO.getStatus().getId() == null) {
                throw new IllegalArgumentException("El estado del ticket no puede ser nulo.");
            }

            // Buscar el ID del estado en la tabla de estados
            TicketStatusEntity status = ticketStatusRepository.findById(Math.toIntExact(ticketDTO.getStatus().getId()))
                    .orElseThrow(() -> new IllegalArgumentException("ID de estado no válido: " + ticketDTO.getStatus().getId()));

            // Actualizar el estado con el ID encontrado
            ticket.setTicketStatusId(Long.valueOf(status.getTicketStatusId()));

            // Manejar la fecha de cierre según el estado
            if (ticket.getTicketStatusId().equals(TicketStatus.COMPLETADO.getId())) {
                ticket.setCloseDate(java.time.LocalDateTime.now());
            } else {
                // Si el estado no es "Completado", la fecha debe ser nula
                ticket.setCloseDate(null);
            }

            // Guardar el ticket para persistir el cambio
            ticketRepository.save(ticket);

            // Retornar el DTO actualizado
            return convertToTicketDTO(ticket);
        } else {
            throw new IllegalArgumentException("Ticket no encontrado con el ID: " + ticketId);
        }
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
                    String techUsername = userTech.getUsername();
                    System.out.println("📤 Notificación enviada al técnico: " + techUsername);
                    messagingTemplate.convertAndSendToUser(techUsername, "/queue/notifications", notificationMessage);
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

                // 🔔 Notificación para el cliente
                String clientUsername = existingTicket.getUserCreator().getUsername();
                String clientMessage = "Tu ticket #" + existingTicket.getTicketId() + " ha sido marcado como completado.";
                System.out.println("📤 Notificación enviada al cliente: " + clientUsername);
                messagingTemplate.convertAndSendToUser(clientUsername, "/queue/notifications", clientMessage);
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

    public void declineTicket(Long ticketId, Long technicianId) {
        // 1. Verificar si el ticket ya fue declinado por este técnico para evitar duplicados.
        boolean alreadyDeclined = declinedTicketRepository.existsByTicketIdAndTechnicianId(ticketId, technicianId);
        if (alreadyDeclined) {
            return; // No hacer nada si ya existe el registro.
        }

        // 2. Crear una nueva entidad y guardarla.
        DeclinedTicketEntity declinedTicket = new DeclinedTicketEntity();
        declinedTicket.setTicketId(ticketId);
        declinedTicket.setTechnicianId(technicianId);
        declinedTicketRepository.save(declinedTicket);
    }



    private TicketDTO convertToTicketDTO(TicketEntity ticket) {
        TicketDTO dto = new TicketDTO();
        dto.setTicketId(ticket.getTicketId());

        // --- ENUMS CORREGIDOS ---

        // CATEGORY: Verificar si el Enum existe antes de usarlo.
        Category categoryEnum = Category.fromIdOptional(ticket.getCategoryId()).orElse(null);
        if (categoryEnum != null) {
            dto.setCategory(new CategoryDTO(categoryEnum.getId(), categoryEnum.getDisplayName()));
        } else {
            // Asignar un valor por defecto o nulo si el ID es inválido/nulo en la DB.
            dto.setCategory(null);
        }

        // PRIORITY: Verificar si el Enum existe antes de usarlo.
        TicketPriority priorityEnum = TicketPriority.fromIdOptional(ticket.getPriorityId()).orElse(null);
        if (priorityEnum != null) {
            dto.setPriority(new TicketPriorityDTO(priorityEnum.getId(), priorityEnum.getDisplayName()));
        } else {
            dto.setPriority(null);
        }

        // STATUS: Verificar si el Enum existe antes de usarlo.
        TicketStatus statusEnum = TicketStatus.fromIdOptional(ticket.getTicketStatusId()).orElse(null);
        if (statusEnum != null) {
            dto.setStatus(new TicketStatusDTO(statusEnum.getId(), statusEnum.getDisplayName()));
        } else {
            dto.setStatus(null);
        }

        // --- USUARIO CREADOR (Ya está bien) ---
        if (ticket.getUserCreator() != null) {
            UserEntity user = ticket.getUserCreator();
            dto.setUserId(user.getUserId());
            dto.setUserName(user.getFullName());
        } else {
            dto.setUserId(null);
            dto.setUserName("Usuario Desconocido");
        }

        // --- TÉCNICO ASIGNADO (Ya está bien) ---
        if (ticket.getAssignedTechUser() != null) {
            UserEntity techUser = ticket.getAssignedTechUser();
            UserDTO techDTO = new UserDTO();
            techDTO.setId(techUser.getUserId());
            techDTO.setDisplayName(techUser.getFullName());
            dto.setAssignedTech(techDTO);
        } else {
            dto.setAssignedTech(null);
        }

        // --- Resto de campos (Ya está bien) ---
        dto.setTitle(ticket.getTitle());
        dto.setDescription(ticket.getDescription());
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

    public Page<TicketDTO> getAssignedTicketsByTechnicianIdPage(Long technicianId, int page, int size) {
        Pageable pageable  = PageRequest.of(page, size);
        Page<TicketEntity> tickets = ticketRepository.findByAssignedTechUser_UserIdPage(technicianId, pageable);
        userRepository.findById(technicianId).orElseThrow(() -> new ExceptionUserNotFound("El id del tecnico " + technicianId + " no existe"));

        return tickets.map(this::convertToTicketDTO);
    }


    public TicketDTO acceptTicket(Long ticketId, Long technicianId) {
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket con ID " + ticketId + " no encontrado."));

        if (ticket.getAssignedTechUser() == null){
            //asignar al tecnico que acepto el ticket
            UserEntity techinicia = userRepository.findById(technicianId)
                    .orElseThrow(() -> new UsernameNotFoundException("El tecnico con el id" + technicianId + "no existe"));
            ticket.setAssignedTechUser(techinicia);
        } else if(!ticket.getAssignedTechUser().getUserId().equals(technicianId)){
            //Si el ticket ya esta asignado a otro tecnico, deniega el acceso
            throw new IllegalArgumentException("El usuario no tiene permiso para aceptar el ticket");
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
        String techUsername = ticket.getAssignedTechUser().getUsername();
        System.out.println("📤 Notificación enviada al técnico: " + techUsername);
        messagingTemplate.convertAndSendToUser(techUsername, "/queue/notifications", notificationMessage);

        String clientUsername = ticket.getUserCreator().getUsername();
        String clientMessage = "Tu ticket #" + ticket.getTicketId() + " ha sido aceptado por un técnico y está en progreso.";
        System.out.println("📤 Notificación enviada al cliente: " + clientUsername);
        messagingTemplate.convertAndSendToUser(clientUsername, "/queue/notifications", clientMessage);


        return convertToTicketDTO(savedTicket);
    }

    public List<TicketDTO> getTicketsEnespera() {
        Long enEsperaId = TicketStatus.EN_ESPERA.getId();

        //Busca todos los tickets que tienen ese id de estado y no tienen un tecnico asignado
        List<TicketEntity> tickets = ticketRepository.findByTicketStatusIdAndAssignedTechUserIsNull(enEsperaId);

        return tickets.stream()
                .map(this::convertToTicketDTO)
                .collect(Collectors.toList());
    }

    public Long countByUserId(Long userId) {
        return ticketRepository.countTicketsByUserId(userId);
    }

}