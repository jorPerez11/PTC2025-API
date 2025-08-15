package H2C_Group.H2C_API.Services;


import H2C_Group.H2C_API.Entities.AuditTrailEntity;
import H2C_Group.H2C_API.Entities.TicketEntity;
import H2C_Group.H2C_API.Entities.UserEntity;
import H2C_Group.H2C_API.Models.DTO.AuditTrailDTO;
import H2C_Group.H2C_API.Repositories.AuditTrailRepository;
import H2C_Group.H2C_API.Repositories.TicketRepository;
import H2C_Group.H2C_API.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditTrailService {
    @Autowired
    private AuditTrailRepository auditTrailRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;


    public Page<AuditTrailDTO> getAllAuditTrails(Pageable pageable) {
        Page<AuditTrailEntity> auditTrail = auditTrailRepository.findAll(pageable);
        return auditTrail.map(this::convertToAuditTrailDTO);
    }

    public AuditTrailDTO createAuditTrail(AuditTrailDTO dto) {
        //Validaciones
        UserEntity existingUser = userRepository.findById(dto.getUserId()).orElseThrow(() -> new IllegalArgumentException("El usuario con id" + dto.getUserId() + " no existe"));

        TicketEntity existingTicket = ticketRepository.findById(dto.getTicketId()).orElseThrow(() -> new IllegalArgumentException("El ticket con id" + dto.getTicketId() + " no existe"));

        AuditTrailEntity auditTrailEntity = new AuditTrailEntity();

        TicketEntity ticket = ticketRepository.findById(dto.getTicketId()).orElseThrow(() -> new IllegalArgumentException("Ticket con ID " + dto.getTicketId() + " no encontrado."));
        auditTrailEntity.setTicket(ticket);

        UserEntity user = userRepository.findById(dto.getUserId()).orElseThrow(() -> new IllegalArgumentException("Usuario con ID " + dto.getUserId() + " no encontrado."));
        auditTrailEntity.setUser(user);

        auditTrailEntity.setChangedField(dto.getChangedField());
        auditTrailEntity.setPreviousValue(dto.getPreviousValue());
        auditTrailEntity.setNewValue(dto.getNewValue());

        auditTrailRepository.save(auditTrailEntity);
        return convertToAuditTrailDTO(auditTrailEntity);
    }


    private AuditTrailDTO convertToAuditTrailDTO(AuditTrailEntity auditTrailEntity) {
        AuditTrailDTO auditTrailDTO = new AuditTrailDTO();

        auditTrailDTO.setAuditTrailId(auditTrailEntity.getHistoryId());

        if  (auditTrailEntity.getUser() != null) {
            auditTrailDTO.setUserId(auditTrailEntity.getUser().getUserId());
        }else {
            throw new IllegalArgumentException("El ID del user no puede ser nulo.");
        }

        if  (auditTrailEntity.getTicket() != null) {
            auditTrailDTO.setTicketId(auditTrailEntity.getTicket().getTicketId());
        }else {
            throw new IllegalArgumentException("El ID del user no puede ser nulo.");
        }

        auditTrailDTO.setChangedField(auditTrailEntity.getChangedField());
        auditTrailDTO.setPreviousValue(auditTrailEntity.getPreviousValue());
        auditTrailDTO.setNewValue(auditTrailEntity.getNewValue());
        return auditTrailDTO;

    }

}
