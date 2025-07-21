package H2C_Group.H2C_API.Services;


import H2C_Group.H2C_API.Entities.NotificationEntity;
import H2C_Group.H2C_API.Entities.TicketEntity;
import H2C_Group.H2C_API.Entities.UserEntity;
import H2C_Group.H2C_API.Exceptions.NotificationExceptions;
import H2C_Group.H2C_API.Exceptions.SolutionExceptions;
import H2C_Group.H2C_API.Models.DTO.NotificationDTO;
import H2C_Group.H2C_API.Repositories.NotificationRepository;
import H2C_Group.H2C_API.Repositories.TicketRepository;
import H2C_Group.H2C_API.Repositories.UserRepository;
import com.fasterxml.jackson.databind.annotation.JsonAppend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    public List<NotificationDTO> findAll() {
        List<NotificationEntity> notifications = notificationRepository.findAll();
        return notifications.stream().map(this::convertToNotificationDTO).collect(Collectors.toList());
    }

    public NotificationDTO createNotification(NotificationDTO notificationDTO) {
        //Validaciones
        UserEntity existingUser = userRepository.findById(notificationDTO.getUserId()).orElseThrow(() -> new IllegalArgumentException("El usuario con id" + notificationDTO.getUserId() + " no existe"));

        TicketEntity existingTicket = ticketRepository.findById(notificationDTO.getTicketId()).orElseThrow(() -> new IllegalArgumentException("La ticket con id" + notificationDTO.getTicketId() + " no existe"));

        NotificationEntity notificationEntity = new NotificationEntity();

        notificationEntity.setUser(existingUser);
        notificationEntity.setTicket(existingTicket);
        if (notificationDTO.getMessage() == null) {
            throw new IllegalArgumentException("El mensaje no puede ser nulo.");
        }
        notificationEntity.setMessage(notificationDTO.getMessage());
        notificationEntity.setSeen(notificationDTO.getSeen());

        notificationRepository.save(notificationEntity);
        return convertToNotificationDTO(notificationEntity);

    }

    public NotificationDTO updateNotification(Long id, NotificationDTO notificationDTO) {
        //Validaciones
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID de la notificacion a actualizar no puede ser nulo o no válido.");
        }

        NotificationEntity existingNotification = notificationRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("El id de la notificacion no puede ser nulo."));

        UserEntity existingUser = userRepository.findById(notificationDTO.getUserId()).orElseThrow(() -> new IllegalArgumentException("El usuario con id" + notificationDTO.getUserId() + " no existe"));
        existingNotification.setUser(existingUser);

        TicketEntity existingTicket = ticketRepository.findById(notificationDTO.getTicketId()).orElseThrow(() -> new IllegalArgumentException("La ticket con id" + notificationDTO.getTicketId() + " no existe"));
        existingNotification.setTicket(existingTicket);

        if (notificationDTO.getMessage() != null) {
            existingNotification.setMessage(notificationDTO.getMessage());
        }

        if (notificationDTO.getSeen() != null) {
            existingNotification.setSeen(notificationDTO.getSeen());
        }

        NotificationEntity savedNotification = notificationRepository.save(existingNotification);
        return convertToNotificationDTO(savedNotification);

    }


    public void deleteNotification(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID de la notificacion no puede ser nulo o no válido");
        }

        boolean exists = notificationRepository.existsById(id);

        if (!exists) {
            throw new NotificationExceptions.NotificationNotFoundException("Notificacion con ID " + id + " no encontrado.");
        }

        notificationRepository.deleteById(id);
    }


    private NotificationDTO convertToNotificationDTO(NotificationEntity notificationEntity) {
        NotificationDTO dto = new NotificationDTO();

        if (notificationEntity.getTicket() != null) {
            dto.setTicketId(notificationEntity.getTicket().getTicketId());
        }else {
            throw new IllegalArgumentException("El ID del ticket no puede ser nulo.");
        }

        if (notificationEntity.getUser() != null) {
            dto.setUserId(notificationEntity.getUser().getUserId());
        }else {
            throw new IllegalArgumentException("El ID del usuario no puede ser nulo.");
        }

        dto.setMessage(notificationEntity.getMessage());
        dto.setSeen(notificationEntity.getSeen());
        return dto;
    }

}
