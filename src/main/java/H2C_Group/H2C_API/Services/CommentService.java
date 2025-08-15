package H2C_Group.H2C_API.Services;

import H2C_Group.H2C_API.Entities.CommentEntity;
import H2C_Group.H2C_API.Entities.TicketEntity;
import H2C_Group.H2C_API.Entities.UserEntity;
import H2C_Group.H2C_API.Exceptions.ExceptionCommentNotFound;
import H2C_Group.H2C_API.Models.DTO.CommentDTO;
import H2C_Group.H2C_API.Repositories.CommentRepository;
import H2C_Group.H2C_API.Repositories.TicketRepository;
import H2C_Group.H2C_API.Repositories.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    public Page<CommentDTO> getAllComments(Pageable pageable) {
        Page<CommentEntity> comments = commentRepository.findAll(pageable);
        return comments.map(this::convertToCommentDTO);
    }

    public CommentDTO createComment(@Valid CommentDTO dto) {
        //Validaciones
        UserEntity existingUser = userRepository.findById(dto.getUserId()).orElseThrow(() -> new IllegalArgumentException("El usuario con id" + dto.getUserId() + " no existe"));

        TicketEntity existingTicket = ticketRepository.findById(dto.getTicketId()).orElseThrow(() -> new IllegalArgumentException("La ticket con id" + dto.getTicketId() + " no existe"));

        CommentEntity comment = new CommentEntity();
        if (dto.getMessage() == null){
            throw new IllegalArgumentException("El comentario no puede ser nulo");
        }

        TicketEntity ticket = ticketRepository.findById(dto.getTicketId()).orElseThrow(() -> new IllegalArgumentException("Ticket con ID " + dto.getTicketId() + " no encontrado."));
        comment.setTicket(ticket);

        UserEntity user = userRepository.findById(dto.getUserId()).orElseThrow(() -> new IllegalArgumentException("Usuario con ID " + dto.getUserId() + " no encontrado."));
        comment.setUser(user);

        comment.setMessage(dto.getMessage());

        CommentEntity savedComment =  commentRepository.save(comment);
        return convertToCommentDTO(savedComment);
    }


    public CommentDTO updateComment(Long id, CommentDTO dto) {
        // 1. Validar el ID del comentario a actualizar
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID del comentario a actualizar no puede ser nulo o no válido.");
        }

        // 2. Buscar el comentario existente
        CommentEntity existingComment = commentRepository.findById(id).orElseThrow(() -> new ExceptionCommentNotFound("Comentario con ID " + id + " no encontrado para actualizar."));

        // 3. Validar y actualizar userId si viene en el DTO
        // Solo actualizamos si el userId viene en el DTO y es diferente al actual
        if (dto.getUserId() != null && !dto.getUserId().equals(existingComment.getUser().getUserId())) {
            UserEntity user = userRepository.findById(dto.getUserId()).orElseThrow(() -> new IllegalArgumentException("El usuario con ID " + dto.getUserId() + " no existe."));
            existingComment.setUser(user); // Asigna la entidad UserEntity
        } else if (dto.getUserId() == null) {
            throw new IllegalArgumentException("El ID del usuario no puede ser nulo al actualizar un comentario.");
        }


        // 4. Validar y actualizar ticketId si viene en el DTO
        // Similar al userId, solo actualizamos si el ticketId viene y es diferente
        if (dto.getTicketId() != null && !dto.getTicketId().equals(existingComment.getTicket().getTicketId())) {
            TicketEntity ticket = ticketRepository.findById(dto.getTicketId()).orElseThrow(() -> new IllegalArgumentException("El ticket con ID " + dto.getTicketId() + " no existe."));
            existingComment.setTicket(ticket); // Asigna la entidad TicketEntity
        } else if (dto.getTicketId() == null) {
            // Similar al userId, ticketId es NOT NULL en DB.
            throw new IllegalArgumentException("El ID del ticket no puede ser nulo al actualizar un comentario.");
        }


        // 5. Validar y actualizar el mensaje
        if (dto.getMessage() != null) { // Si el mensaje viene en el DTO (no es null)
            String trimmedMessage = dto.getMessage().trim();
            if (trimmedMessage.isBlank()) { // Si después de trim es vacío
                throw new IllegalArgumentException("El mensaje del comentario no puede ser vacío.");
            } else if (trimmedMessage.length() > 1000) {
                throw new IllegalArgumentException("El texto del comentario actualizado excede la longitud máxima permitida (1000 caracteres).");
            } else {
                existingComment.setMessage(trimmedMessage); // Actualiza el mensaje
            }
        }
        // Si dto.getMessage() es null, no hacemos nada, conservando el mensaje existente.

        CommentEntity updatedComment = commentRepository.save(existingComment);
        return convertToCommentDTO(updatedComment);
    }

    public void deleteComment(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID del comentario no puede ser nulo o no válido");
        }

        boolean exists = commentRepository.existsById(id);

        if (!exists) {
            throw new ExceptionCommentNotFound("Comentario con ID " + id + " no encontrado.");
        }

        commentRepository.deleteById(id);

    }


    private CommentDTO convertToCommentDTO(CommentEntity commentEntity) {
        CommentDTO  commentDTO = new CommentDTO();
        commentDTO.setCommentId(commentEntity.getCommentId());

        if (commentEntity.getTicket() != null) {
            commentDTO.setTicketId(commentEntity.getTicket().getTicketId());
        }else {
            throw new IllegalArgumentException("El ID del ticket no puede ser nulo.");
        }

        if  (commentEntity.getUser() != null) {
            commentDTO.setUserId(commentEntity.getUser().getUserId());
        }else {
            throw new IllegalArgumentException("El ID del user no puede ser nulo.");
        }

        commentDTO.setMessage(commentEntity.getMessage());
        return commentDTO;
    }

}
