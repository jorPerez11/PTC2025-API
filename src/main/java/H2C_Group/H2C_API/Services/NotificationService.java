package H2C_Group.H2C_API.Services;


import H2C_Group.H2C_API.Entities.NotificationEntity;
import H2C_Group.H2C_API.Entities.TicketEntity;
import H2C_Group.H2C_API.Entities.UserEntity;
import H2C_Group.H2C_API.Exceptions.ExceptionNotificationNotFound;
import H2C_Group.H2C_API.Models.DTO.NotificationDTO;
import H2C_Group.H2C_API.Repositories.NotificationRepository;
import H2C_Group.H2C_API.Repositories.TicketRepository;
import H2C_Group.H2C_API.Repositories.UserRepository;
import com.fasterxml.jackson.databind.annotation.JsonAppend;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public Page<NotificationDTO> findAll(Pageable pageable) {
        Page<NotificationEntity> notifications = notificationRepository.findAll(pageable);
        return notifications.map(this::convertToNotificationDTO);
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

    public void markAllAsSeenForUser(Long userId) {

    }


    //Metodo de actualizacion del estado de notificacion (seen == 1)
    public NotificationDTO updateNotificationState(Long id, NotificationDTO notificationDTO) {
        //Validaciones
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El notification no puede ser nulo.");
        }

        NotificationEntity existingNotification = notificationRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("El id de la notificacion " + id + " no existe."));

        if(existingNotification.getSeen() == 0) {
            existingNotification.setSeen(1);
            NotificationEntity updatedNotification = notificationRepository.save(existingNotification);
            return convertToNotificationDTO(updatedNotification);
        }

        return convertToNotificationDTO(existingNotification);

    }


    public void deleteNotification(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID de la notificacion no puede ser nulo o no válido");
        }

        boolean exists = notificationRepository.existsById(id);

        if (!exists) {
            throw new ExceptionNotificationNotFound("Notificacion con ID " + id + " no encontrado.");
        }

        notificationRepository.deleteById(id);
    }


    private NotificationDTO convertToNotificationDTO(NotificationEntity notificationEntity) {
        NotificationDTO dto = new NotificationDTO();

        dto.setNotificationId(notificationEntity.getNotificationId());

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
