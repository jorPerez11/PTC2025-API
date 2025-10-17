package H2C_Group.H2C_API.Services;

import H2C_Group.H2C_API.Entities.Views.TicketDetailView;
import H2C_Group.H2C_API.Entities.Views.TicketView;
import H2C_Group.H2C_API.Enums.TicketStatus;
import H2C_Group.H2C_API.Models.DTO.TicketViewDTO;
import H2C_Group.H2C_API.Repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TicketViewService {

    @Autowired
    private TicketDetailViewRepository ticketDetailViewRepository;

    @Autowired
    private TicketViewRepository ticketViewRepository;

    @Autowired
    private FullTicketDataViewRepository fullTicketDataViewRepository; // Ahora inyecta el nuevo repositorio

    public List<TicketView> getAllTickets() {
        return ticketViewRepository.findAll();
    }

    public Optional<TicketDetailView> getTicketDetails(Long ticketId) {
        return ticketDetailViewRepository.findByTicketId(ticketId);
    }

    // Método corregido para obtener tickets por ID de técnico
    public List<TicketViewDTO> getTicketsByAssignedTech(Long techId) {
        List<TicketView> tickets = fullTicketDataViewRepository.findByAssignedTech(techId);
        return tickets.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private TicketViewDTO convertToDto(TicketView view) {
        TicketViewDTO dto = new TicketViewDTO();
        dto.setTicketId(view.getTicketId());
        dto.setAsunto(view.getAsunto());
        dto.setFullName(view.getFullName());

        // Convertimos el java.util.Date a java.sql.Timestamp
        // La forma más segura es usar el constructor de Timestamp
        if (view.getConsultDate() != null) {
            dto.setConsultDate(new Timestamp(view.getConsultDate().getTime()));
        } else {
            dto.setConsultDate(null);
        }

        dto.setTicketStatus(view.getTicketStatus());
        dto.setPhotoUrl(view.getPhotoUrl());
        return dto;
    }
}
